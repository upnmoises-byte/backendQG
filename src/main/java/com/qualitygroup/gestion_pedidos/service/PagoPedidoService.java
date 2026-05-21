package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.dto.RegistrarPagoRequest;
import com.qualitygroup.gestion_pedidos.model.PagoPedido;
import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.PagoPedidoRepository;
import com.qualitygroup.gestion_pedidos.repository.PedidoRepository;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class PagoPedidoService {

    private static final Set<String> METODOS = new HashSet<>(Arrays.asList(
            "BCP", "YAPE", "BBVA", "EFECTIVO", "VISA"
    ));

    private static final String METODO_EFECTIVO = "EFECTIVO";

    /** Ventana para rechazar el mismo abono por doble clic. */
    private static final int SEGUNDOS_ANTIDUPLICADO = 12;

    private final PagoPedidoRepository pagoPedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagoPedidoService(
            PagoPedidoRepository pagoPedidoRepository,
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.pagoPedidoRepository = pagoPedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PagoPedido> listarPorPedido(Long pedidoId) {
        return pagoPedidoRepository.listarPorPedidoId(pedidoId);
    }

    @Transactional
    public PagoPedido registrar(Long pedidoId, RegistrarPagoRequest req) {
        assertPuedeRegistrarPago();

        if (req.getMonto() == null || req.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        if (req.getMetodoPago() == null || req.getMetodoPago().isBlank()) {
            throw new IllegalArgumentException("Indique el método de pago");
        }

        String metodo = req.getMetodoPago().trim().toUpperCase();
        if (!METODOS.contains(metodo)) {
            throw new IllegalArgumentException("Método de pago no válido");
        }

        String codigoPago = normalizarCodigoPago(metodo, req.getCodigoPago());

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        assertNoDuplicadoReciente(pedidoId, req.getMonto(), metodo, codigoPago);

        BigDecimal adelantoActual = pedido.getAdelanto() != null ? pedido.getAdelanto() : BigDecimal.ZERO;
        pedido.setAdelanto(adelantoActual.add(req.getMonto()));
        pedidoRepository.save(pedido);

        PagoPedido p = new PagoPedido();
        p.setPedido(pedido);
        p.setMonto(req.getMonto());
        p.setMetodoPago(metodo);
        p.setCodigoPago(codigoPago);
        p.setNota(req.getNota() != null ? req.getNota().trim() : null);
        p.setFechaRegistro(LocalDate.now());
        p.setHoraRegistro(LocalTime.now());

        UsuarioAuditoria ua = leerUsuarioActual();
        p.setUsuarioNombre(ua.nombre());
        p.setUsuarioCorreo(ua.correo());
        p.setUsuarioRol(ua.rol());

        return pagoPedidoRepository.save(p);
    }

    private static String normalizarCodigoPago(String metodo, String codigoRaw) {
        if (METODO_EFECTIVO.equals(metodo)) {
            return null;
        }
        if (codigoRaw == null || codigoRaw.isBlank()) {
            throw new IllegalArgumentException("Indique el código de operación del pago");
        }
        String codigo = codigoRaw.trim();
        if (codigo.length() < 3 || codigo.length() > 64) {
            throw new IllegalArgumentException("El código de operación debe tener entre 3 y 64 caracteres");
        }
        return codigo;
    }

    private void assertNoDuplicadoReciente(
            Long pedidoId,
            BigDecimal monto,
            String metodo,
            String codigoPago
    ) {
        Optional<PagoPedido> ultimo = pagoPedidoRepository.findFirstByPedido_IdOrderByIdDesc(pedidoId);
        if (ultimo.isEmpty()) {
            return;
        }
        PagoPedido u = ultimo.get();
        if (u.getMonto() == null || u.getMonto().compareTo(monto) != 0 || !metodo.equals(u.getMetodoPago())) {
            return;
        }
        if (!Objects.equals(
                codigoPago != null ? codigoPago : "",
                u.getCodigoPago() != null ? u.getCodigoPago() : ""
        )) {
            return;
        }
        if (u.getFechaRegistro() == null || u.getHoraRegistro() == null) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        if (!hoy.equals(u.getFechaRegistro())) {
            return;
        }
        long segundos = Duration.between(u.getHoraRegistro(), LocalTime.now()).getSeconds();
        if (segundos >= 0 && segundos <= SEGUNDOS_ANTIDUPLICADO) {
            throw new IllegalArgumentException("Este abono ya fue registrado. Evite hacer doble clic en Guardar.");
        }
    }

    private void assertPuedeRegistrarPago() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("No autenticado");
        }
        boolean ok = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_PRODUCCION") || a.equals("ROLE_CAJA"));
        if (!ok) {
            throw new SecurityException("No tiene permiso para registrar pagos");
        }
    }

    private UsuarioAuditoria leerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : "";
        String nombre = correo;
        String rol = "";
        if (auth != null) {
            rol = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst()
                    .map(a -> a.replace("ROLE_", ""))
                    .orElse("");
            nombre = usuarioRepository.findByCorreo(correo)
                    .map(Usuario::getNombre)
                    .filter(n -> n != null && !n.isBlank())
                    .orElse(correo);
        }
        return new UsuarioAuditoria(nombre, correo, rol);
    }

    private record UsuarioAuditoria(String nombre, String correo, String rol) {
    }
}
