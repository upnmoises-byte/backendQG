package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByVendedora(String vendedora);

    List<Pedido> findByMaquina(String maquina);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.cliente LEFT JOIN FETCH p.detalles")
    List<Pedido> findAllWithDetalles();
}