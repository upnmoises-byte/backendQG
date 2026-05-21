package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByVendedora(String vendedora);

    List<Pedido> findByMaquina(String maquina);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.cliente LEFT JOIN FETCH p.detalles")
    List<Pedido> findAllWithDetalles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);
}