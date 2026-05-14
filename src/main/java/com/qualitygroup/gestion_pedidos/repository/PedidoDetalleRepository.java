package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository
        extends JpaRepository<PedidoDetalle, Long> {
}