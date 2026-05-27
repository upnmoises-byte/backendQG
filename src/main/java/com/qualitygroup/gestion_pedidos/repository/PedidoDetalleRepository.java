package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {

    @Query("SELECT COUNT(d) > 0 FROM PedidoDetalle d WHERE UPPER(TRIM(d.material)) = UPPER(TRIM(:nombre))")
    boolean existsByMaterialNombre(@Param("nombre") String nombre);
}
