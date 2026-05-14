package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.PedidoDetalleEspecial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleEspecialRepository
        extends JpaRepository<PedidoDetalleEspecial, Long> {
}