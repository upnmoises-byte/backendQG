package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.ActualizarEstadoDetalleRequest;
import com.qualitygroup.gestion_pedidos.dto.PagoPedidoDto;
import com.qualitygroup.gestion_pedidos.dto.RegistrarPagoRequest;
import com.qualitygroup.gestion_pedidos.model.AuditoriaPedido;
import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.repository.AuditoriaPedidoRepository;
import com.qualitygroup.gestion_pedidos.repository.ClienteRepository;
import com.qualitygroup.gestion_pedidos.service.PagoPedidoService;
import com.qualitygroup.gestion_pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize(com.qualitygroup.gestion_pedidos.security.AppRoles.HAS_ANY_APP_ROLE)
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    private final PedidoService pedidoService;
    private final AuditoriaPedidoRepository auditoriaRepository;
    private final ClienteRepository clienteRepository;
    private final ObjectMapper objectMapper;
    private final PagoPedidoService pagoPedidoService;

    public PedidoController(
            PedidoService pedidoService,
            AuditoriaPedidoRepository auditoriaRepository,
            ClienteRepository clienteRepository,
            ObjectMapper objectMapper,
            PagoPedidoService pagoPedidoService
    ) {
        this.pedidoService = pedidoService;
        this.auditoriaRepository = auditoriaRepository;
        this.clienteRepository = clienteRepository;
        this.objectMapper = objectMapper;
        this.pagoPedidoService = pagoPedidoService;
    }

    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/estado/{estado}")
    public List<Pedido> listarPorEstado(@PathVariable String estado) {
        return pedidoService.listarPorEstado(estado);
    }

    @GetMapping("/siguiente-numero")
    public Map<String, String> siguienteNumero() {
        return Map.of("numeroOrden", pedidoService.siguienteNumeroOrden());
    }

    @GetMapping("/{id}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCCION','CAJA')")
    public List<PagoPedidoDto> listarPagos(@PathVariable Long id) {
        pedidoService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        return pagoPedidoService.listarPorPedido(id).stream()
                .map(PagoPedidoDto::fromEntity)
                .toList();
    }

    @PostMapping("/{id}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCCION','CAJA')")
    public PagoPedidoDto registrarPago(@PathVariable Long id, @RequestBody RegistrarPagoRequest body) {
        return PagoPedidoDto.fromEntity(pagoPedidoService.registrar(id, body));
    }

    @GetMapping("/{id}/auditoria")
    public List<AuditoriaPedido> listarAuditoria(@PathVariable Long id) {
        return auditoriaRepository.findByPedidoIdOrderByFechaCambioDescHoraCambioDesc(id);
    }

    @PostMapping
    public Pedido crear(@RequestBody Pedido pedido) {
        return pedidoService.guardar(pedido);
    }

    @PutMapping("/{pedidoId}/detalles/{detalleId}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCCION')")
    public Pedido actualizarEstadoDetalle(
            @PathVariable Long pedidoId,
            @PathVariable Long detalleId,
            @RequestBody ActualizarEstadoDetalleRequest body
    ) {
        log.info("Actualizar estado detalle: pedidoId={}, detalleId={}, estado={}", pedidoId, detalleId, body.getEstado());
        return pedidoService.actualizarEstadoDetalle(
                pedidoId,
                detalleId,
                body.getEstado(),
                body.getUsuarioNombre(),
                body.getUsuarioCorreo(),
                body.getUsuarioRol()
        );
    }

    @PutMapping("/{id}")
    public Pedido actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {

        String usuarioNombre = (String) body.remove("usuarioNombre");
        String usuarioCorreo = (String) body.remove("usuarioCorreo");
        String usuarioRol = (String) body.remove("usuarioRol");

        Pedido pedido = objectMapper.convertValue(body, Pedido.class);

        if (body.get("cliente") != null) {
            Map<String, Object> clienteMap = (Map<String, Object>) body.get("cliente");
            Long clienteId = Long.valueOf(clienteMap.get("id").toString());

            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            pedido.setCliente(cliente);
        }

        return pedidoService.actualizar(
                id,
                pedido,
                usuarioNombre,
                usuarioCorreo,
                usuarioRol
        );
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
    }
}