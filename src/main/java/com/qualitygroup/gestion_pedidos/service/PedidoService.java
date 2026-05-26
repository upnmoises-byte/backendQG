package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.AuditoriaPedido;
import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.model.PedidoDetalle;
import com.qualitygroup.gestion_pedidos.model.PedidoDetalleEspecial;
import com.qualitygroup.gestion_pedidos.repository.AuditoriaPedidoRepository;
import com.qualitygroup.gestion_pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class PedidoService {

    private static final String ESTADO_DEFECTO = "CORTE";

    private final PedidoRepository pedidoRepository;
    private final AuditoriaPedidoRepository auditoriaRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            AuditoriaPedidoRepository auditoriaRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public String siguienteNumeroOrden(String serieRaw) {
        String serie = (serieRaw == null || serieRaw.isBlank()) ? "27000" : serieRaw.trim();
        int base;
        try {
            base = Integer.parseInt(serie);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Serie inválida");
        }
        String prefijo = serie.length() >= 2 ? serie.substring(0, 2) : serie;
        int max = pedidoRepository.findByNumeroOrdenStartingWith(prefijo).stream()
                .map(Pedido::getNumeroOrden)
                .filter(n -> n != null && n.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(base);
        return String.valueOf(Math.max(max, base) + 1);
    }

    public Pedido guardar(Pedido pedido) {

        if (pedido.getFechaIngreso() == null) {
            pedido.setFechaIngreso(LocalDate.now());
        }

        if (pedido.getHoraIngreso() == null) {
            pedido.setHoraIngreso(LocalTime.now());
        }

        if (pedido.getPrioridad() == null) {
            pedido.setPrioridad(0);
        }

        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
            pedido.setEstado(ESTADO_DEFECTO);
        }

        if (pedido.getDetalles() != null) {
            pedido.getDetalles().forEach(detalle -> {
                detalle.setPedido(pedido);
                if (detalle.getEstado() == null || detalle.getEstado().isBlank()) {
                    detalle.setEstado(pedido.getEstado());
                }
                vincularEspeciales(detalle);
            });
            sincronizarEstadoPedido(pedido);
        }

        normalizarPedido(pedido);
        validarPedido(pedido, null);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(
            Long id,
            Pedido pedidoActualizado,
            String usuarioNombre,
            String usuarioCorreo,
            String usuarioRol
    ) {

        Pedido actual = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        String estadoAnteriorPedido = actual.getEstado();
        Map<Long, String> estadosDetalleAntes = mapaEstadosDetalle(actual);

        actual.setNumeroOrden(pedidoActualizado.getNumeroOrden());
        actual.setCliente(pedidoActualizado.getCliente());
        actual.setVendedora(pedidoActualizado.getVendedora());
        actual.setObservaciones(pedidoActualizado.getObservaciones());
        actual.setFechaEntrega(pedidoActualizado.getFechaEntrega());
        actual.setHoraEntrega(pedidoActualizado.getHoraEntrega());
        actual.setPrioridad(pedidoActualizado.getPrioridad());
        actual.setTotal(pedidoActualizado.getTotal());
        actual.setAdelanto(pedidoActualizado.getAdelanto());

        actual.setCantidad(pedidoActualizado.getCantidad());
        actual.setColorPrincipal(pedidoActualizado.getColorPrincipal());
        actual.setColorSecundario(pedidoActualizado.getColorSecundario());
        actual.setColorTercero(pedidoActualizado.getColorTercero());
        actual.setCortes(pedidoActualizado.getCortes());
        actual.setRanuras(pedidoActualizado.getRanuras());
        actual.setPerforaciones(pedidoActualizado.getPerforaciones());
        actual.setMaquina(pedidoActualizado.getMaquina());
        actual.setCantoDelgado(pedidoActualizado.getCantoDelgado());
        actual.setCantoGrueso(pedidoActualizado.getCantoGrueso());
        actual.setCantoDelgado36mm(pedidoActualizado.getCantoDelgado36mm());
        actual.setCantoGrueso36mm(pedidoActualizado.getCantoGrueso36mm());
        actual.setCantidadEspeciales(pedidoActualizado.getCantidadEspeciales());

        if (pedidoActualizado.getDetalles() != null) {
            fusionarDetalles(actual, pedidoActualizado.getDetalles());
            sincronizarEstadoPedido(actual);
        } else if (pedidoActualizado.getEstado() != null) {
            actual.setEstado(pedidoActualizado.getEstado());
            if (actual.getDetalles() != null) {
                for (PedidoDetalle d : actual.getDetalles()) {
                    d.setEstado(pedidoActualizado.getEstado());
                }
            }
        }

        normalizarPedido(actual);
        validarPedido(actual, id);
        Pedido guardado = pedidoRepository.save(actual);

        registrarAuditoriaCambiosEstado(
                guardado,
                estadoAnteriorPedido,
                estadosDetalleAntes,
                usuarioNombre,
                usuarioCorreo,
                usuarioRol
        );

        return guardado;
    }

    private void fusionarDetalles(Pedido actual, List<PedidoDetalle> detallesEntrada) {
        Map<Long, PedidoDetalle> existentesPorId = new HashMap<>();
        for (PedidoDetalle d : actual.getDetalles()) {
            if (d.getId() != null) {
                existentesPorId.put(d.getId(), d);
            }
        }

        List<PedidoDetalle> nuevos = new ArrayList<>();

        for (PedidoDetalle entrada : detallesEntrada) {
            if (entrada.getId() != null && existentesPorId.containsKey(entrada.getId())) {
                PedidoDetalle existente = existentesPorId.get(entrada.getId());
                copiarCamposDetalle(existente, entrada, actual.getEstado());
                existente.setPedido(actual);
                vincularEspeciales(existente);
                nuevos.add(existente);
            } else {
                entrada.setId(null);
                entrada.setPedido(actual);
                if (entrada.getEstado() == null || entrada.getEstado().isBlank()) {
                    entrada.setEstado(actual.getEstado() != null ? actual.getEstado() : ESTADO_DEFECTO);
                }
                vincularEspeciales(entrada);
                nuevos.add(entrada);
            }
        }

        actual.getDetalles().clear();
        actual.getDetalles().addAll(nuevos);
    }

    private static void copiarCamposDetalle(PedidoDetalle destino, PedidoDetalle origen, String estadoPedidoFallback) {
        destino.setCantidad(origen.getCantidad());
        destino.setMaterial(origen.getMaterial());
        destino.setMaquina(origen.getMaquina());
        if (origen.getEstado() != null && !origen.getEstado().isBlank()) {
            destino.setEstado(origen.getEstado());
        }
        destino.setCortes(origen.getCortes());
        destino.setRanuras(origen.getRanuras());
        destino.setPerforaciones(origen.getPerforaciones());
        destino.setCantoDelgado(origen.getCantoDelgado());
        destino.setCantoGrueso(origen.getCantoGrueso());
        destino.setCantoDelgado36mm(origen.getCantoDelgado36mm());
        destino.setCantoGrueso36mm(origen.getCantoGrueso36mm());
        destino.setObservaciones(origen.getObservaciones());

        if (origen.getEspeciales() != null) {
            destino.getEspeciales().clear();
            for (PedidoDetalleEspecial e : origen.getEspeciales()) {
                PedidoDetalleEspecial copia = new PedidoDetalleEspecial();
                copia.setCantidad(e.getCantidad());
                copia.setDescripcion(e.getDescripcion());
                copia.setDetalle(destino);
                destino.getEspeciales().add(copia);
            }
        }
    }

    private static void vincularEspeciales(PedidoDetalle detalle) {
        if (detalle.getEspeciales() != null) {
            detalle.getEspeciales().forEach(especial -> especial.setDetalle(detalle));
        }
    }

    private static void normalizarPedido(Pedido pedido) {
        if (pedido.getNumeroOrden() != null) {
            pedido.setNumeroOrden(pedido.getNumeroOrden().trim());
        }
        if (pedido.getVendedora() != null) {
            pedido.setVendedora(pedido.getVendedora().trim().toUpperCase());
        }
        if (pedido.getColorPrincipal() != null) {
            pedido.setColorPrincipal(pedido.getColorPrincipal().trim().toUpperCase());
        }
        if (pedido.getColorSecundario() != null) {
            pedido.setColorSecundario(pedido.getColorSecundario().trim().toUpperCase());
        }
        if (pedido.getColorTercero() != null) {
            pedido.setColorTercero(pedido.getColorTercero().trim().toUpperCase());
        }
        if (pedido.getDetalles() != null) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                if (detalle.getMaterial() != null) {
                    detalle.setMaterial(detalle.getMaterial().trim().toUpperCase());
                }
                if (detalle.getEspeciales() != null) {
                    for (PedidoDetalleEspecial especial : detalle.getEspeciales()) {
                        if (especial.getDescripcion() != null) {
                            especial.setDescripcion(especial.getDescripcion().trim().toUpperCase());
                        }
                    }
                }
            }
        }
    }

    private void validarPedido(Pedido pedido, Long idActual) {
        if (pedido.getNumeroOrden() == null || pedido.getNumeroOrden().isBlank()) {
            throw new IllegalArgumentException("Ingrese el N° de orden");
        }
        pedidoRepository.findFirstByNumeroOrden(pedido.getNumeroOrden().trim()).ifPresent(existente -> {
            if (idActual == null || !Objects.equals(existente.getId(), idActual)) {
                throw new IllegalArgumentException("Ya existe un pedido registrado con este número de orden.");
            }
        });
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            throw new IllegalArgumentException("Seleccione un cliente");
        }
        if (pedido.getVendedora() == null || pedido.getVendedora().isBlank()) {
            throw new IllegalArgumentException("Seleccione una vendedora");
        }
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal adelanto = pedido.getAdelanto() != null ? pedido.getAdelanto() : BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) < 0 || adelanto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total y adelanto no pueden ser negativos");
        }
        if (adelanto.compareTo(total) > 0) {
            throw new IllegalArgumentException("El adelanto no puede ser mayor al total del pedido");
        }
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un material al pedido");
        }
        boolean intentaEntregado = "ENTREGADO".equalsIgnoreCase(pedido.getEstado())
                || pedido.getDetalles().stream().anyMatch(d -> "ENTREGADO".equalsIgnoreCase(d.getEstado()));
        if (intentaEntregado && saldoPedido(pedido).compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("No puede pasar a ENTREGADO con saldo pendiente. Cancele la deuda o registre abonos antes.");
        }
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            if (detalle.getMaterial() == null || detalle.getMaterial().isBlank()) {
                throw new IllegalArgumentException("Cada material debe tener nombre/color");
            }
            BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada material debe tener cantidad mayor a 0");
            }
            if (detalle.getMaquina() == null || detalle.getMaquina().isBlank()) {
                throw new IllegalArgumentException("Cada material debe tener máquina asignada");
            }
        }
    }

    private static BigDecimal saldoPedido(Pedido pedido) {
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal adelanto = pedido.getAdelanto() != null ? pedido.getAdelanto() : BigDecimal.ZERO;
        BigDecimal saldo = total.subtract(adelanto);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    /** Estado resumen del pedido según los materiales. */
    static void sincronizarEstadoPedido(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
                pedido.setEstado(ESTADO_DEFECTO);
            }
            return;
        }

        List<String> estados = pedido.getDetalles().stream()
                .map(d -> d.getEstado() != null && !d.getEstado().isBlank()
                        ? d.getEstado()
                        : (pedido.getEstado() != null ? pedido.getEstado() : ESTADO_DEFECTO))
                .distinct()
                .toList();

        if (estados.size() == 1) {
            pedido.setEstado(estados.get(0));
            return;
        }

        boolean todosEntregados = pedido.getDetalles().stream()
                .allMatch(d -> "ENTREGADO".equalsIgnoreCase(estadoDetalle(d, pedido)));
        if (todosEntregados) {
            pedido.setEstado("ENTREGADO");
            return;
        }

        pedido.setEstado("EN_PROCESO");
    }

    private static String estadoDetalle(PedidoDetalle d, Pedido pedido) {
        if (d.getEstado() != null && !d.getEstado().isBlank()) {
            return d.getEstado();
        }
        return pedido.getEstado() != null ? pedido.getEstado() : ESTADO_DEFECTO;
    }

    private static Map<Long, String> mapaEstadosDetalle(Pedido pedido) {
        Map<Long, String> mapa = new HashMap<>();
        if (pedido.getDetalles() != null) {
            for (PedidoDetalle d : pedido.getDetalles()) {
                if (d.getId() != null) {
                    mapa.put(d.getId(), estadoDetalle(d, pedido));
                }
            }
        }
        return mapa;
    }

    private void registrarAuditoriaCambiosEstado(
            Pedido guardado,
            String estadoAnteriorPedido,
            Map<Long, String> estadosDetalleAntes,
            String usuarioNombre,
            String usuarioCorreo,
            String usuarioRol
    ) {
        String estadoNuevoPedido = guardado.getEstado();

        if (!Objects.equals(estadoAnteriorPedido, estadoNuevoPedido)
                && (guardado.getDetalles() == null || guardado.getDetalles().isEmpty())) {
            guardarAuditoria(
                    guardado,
                    estadoAnteriorPedido,
                    estadoNuevoPedido,
                    usuarioNombre,
                    usuarioCorreo,
                    usuarioRol
            );
        }

        if (guardado.getDetalles() != null) {
            for (PedidoDetalle d : guardado.getDetalles()) {
                if (d.getId() == null) {
                    continue;
                }
                String antes = estadosDetalleAntes.get(d.getId());
                String ahora = estadoDetalle(d, guardado);
                if (antes != null && !antes.equals(ahora)) {
                    String material = d.getMaterial() != null ? d.getMaterial() : "Material";
                    guardarAuditoria(
                            guardado,
                            antes,
                            ahora + " · " + material,
                            usuarioNombre,
                            usuarioCorreo,
                            usuarioRol
                    );
                }
            }
        }
    }

    private void guardarAuditoria(
            Pedido pedido,
            String estadoAnterior,
            String estadoNuevo,
            String usuarioNombre,
            String usuarioCorreo,
            String usuarioRol
    ) {
        AuditoriaPedido auditoria = new AuditoriaPedido();
        auditoria.setPedidoId(pedido.getId());
        auditoria.setNumeroOrden(pedido.getNumeroOrden());
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setUsuarioNombre(usuarioNombre);
        auditoria.setUsuarioCorreo(usuarioCorreo);
        auditoria.setUsuarioRol(usuarioRol);
        auditoria.setFechaCambio(LocalDate.now());
        auditoria.setHoraCambio(LocalTime.now());
        auditoriaRepository.save(auditoria);
    }

    @Transactional
    public Pedido actualizarEstadoDetalle(
            Long pedidoId,
            Long detalleId,
            String nuevoEstado,
            String usuarioNombre,
            String usuarioCorreo,
            String usuarioRol
    ) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("Indique el estado");
        }
        String estadoNorm = nuevoEstado.trim().toUpperCase();

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if ("ENTREGADO".equals(estadoNorm) && saldoPedido(pedido).compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("No puede pasar a ENTREGADO con saldo pendiente. Cancele la deuda o registre abonos antes.");
        }

        PedidoDetalle detalle = pedido.getDetalles().stream()
                .filter(d -> detalleId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado en el pedido"));

        String estadoAnterior = estadoDetalle(detalle, pedido);
        if (estadoAnterior.equals(estadoNorm)) {
            return pedido;
        }

        detalle.setEstado(estadoNorm);
        sincronizarEstadoPedido(pedido);
        Pedido guardado = pedidoRepository.save(pedido);

        String material = detalle.getMaterial() != null ? detalle.getMaterial() : "Material";
        guardarAuditoria(
                guardado,
                estadoAnterior,
                estadoNorm + " · " + material,
                usuarioNombre,
                usuarioCorreo,
                usuarioRol
        );

        return guardado;
    }

    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }
}
