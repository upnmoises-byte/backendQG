package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.PagoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PagoPedidoRepository extends JpaRepository<PagoPedido, Long> {

    @Query("SELECT p FROM PagoPedido p WHERE p.pedido.id = :pedidoId ORDER BY p.fechaRegistro DESC, p.horaRegistro DESC")
    List<PagoPedido> listarPorPedidoId(@Param("pedidoId") Long pedidoId);

    Optional<PagoPedido> findFirstByPedido_IdOrderByIdDesc(Long pedidoId);
}
