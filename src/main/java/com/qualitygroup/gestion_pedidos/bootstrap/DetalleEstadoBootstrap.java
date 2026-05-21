package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.model.PedidoDetalle;
import com.qualitygroup.gestion_pedidos.repository.PedidoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asigna estado a detalles existentes que aún no lo tienen (hereda del pedido).
 */
@Component
@Order(5)
public class DetalleEstadoBootstrap implements CommandLineRunner {

    private final PedidoRepository pedidoRepository;

    public DetalleEstadoBootstrap(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Pedido pedido : pedidoRepository.findAll()) {
            String fallback = pedido.getEstado() != null && !pedido.getEstado().isBlank()
                    ? pedido.getEstado()
                    : "CORTE";
            boolean cambio = false;
            if (pedido.getDetalles() != null) {
                for (PedidoDetalle d : pedido.getDetalles()) {
                    if (d.getEstado() == null || d.getEstado().isBlank()) {
                        d.setEstado(fallback);
                        cambio = true;
                    }
                }
            }
            if (cambio) {
                pedidoRepository.save(pedido);
            }
        }
    }
}
