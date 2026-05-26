package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.AuditoriaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaPedidoRepository extends JpaRepository<AuditoriaPedido, Long> {

    List<AuditoriaPedido> findByPedidoIdOrderByFechaCambioDescHoraCambioDesc(Long pedidoId);

    void deleteByPedidoId(Long pedidoId);
}