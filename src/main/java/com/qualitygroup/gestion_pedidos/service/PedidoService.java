package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.AuditoriaPedido;
import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.repository.AuditoriaPedidoRepository;
import com.qualitygroup.gestion_pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class PedidoService {

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

        if (pedido.getDetalles() != null) {

            pedido.getDetalles().forEach(detalle -> {

                detalle.setPedido(pedido);

                if (detalle.getEspeciales() != null) {
                    detalle.getEspeciales().forEach(especial -> {
                        especial.setDetalle(detalle);
                    });
                }

            });

        }

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

        String estadoAnterior = actual.getEstado();

        actual.setNumeroOrden(pedidoActualizado.getNumeroOrden());
        actual.setCliente(pedidoActualizado.getCliente());
        actual.setEstado(pedidoActualizado.getEstado());
        actual.setVendedora(pedidoActualizado.getVendedora());
        actual.setObservaciones(pedidoActualizado.getObservaciones());
        actual.setFechaEntrega(pedidoActualizado.getFechaEntrega());
        actual.setHoraEntrega(pedidoActualizado.getHoraEntrega());
        actual.setPrioridad(pedidoActualizado.getPrioridad());
        actual.setTotal(pedidoActualizado.getTotal());
        actual.setAdelanto(pedidoActualizado.getAdelanto());

        // Campos resumen para compatibilidad con tablas antiguas
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
            actual.getDetalles().clear();

            pedidoActualizado.getDetalles().forEach(detalle -> {
                detalle.setId(null);
                detalle.setPedido(actual);

                if (detalle.getEspeciales() != null) {
                    detalle.getEspeciales().forEach(especial -> {
                        especial.setId(null);
                        especial.setDetalle(detalle);
                    });
                }

                actual.getDetalles().add(detalle);
            });
        }

        Pedido guardado = pedidoRepository.save(actual);

        if (estadoAnterior != null && pedidoActualizado.getEstado() != null && !estadoAnterior.equals(pedidoActualizado.getEstado())) {

            AuditoriaPedido auditoria = new AuditoriaPedido();

            auditoria.setPedidoId(guardado.getId());
            auditoria.setNumeroOrden(guardado.getNumeroOrden());

            auditoria.setEstadoAnterior(estadoAnterior);
            auditoria.setEstadoNuevo(pedidoActualizado.getEstado());

            auditoria.setUsuarioNombre(usuarioNombre);
            auditoria.setUsuarioCorreo(usuarioCorreo);
            auditoria.setUsuarioRol(usuarioRol);

            auditoria.setFechaCambio(LocalDate.now());
            auditoria.setHoraCambio(LocalTime.now());

            auditoriaRepository.save(auditoria);
        }

        return guardado;
    }

    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }
}