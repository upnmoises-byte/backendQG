package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.PedidoDetalleEspecial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoDetalleEspecialRepository extends JpaRepository<PedidoDetalleEspecial, Long> {

    @Query("SELECT COUNT(e) > 0 FROM PedidoDetalleEspecial e WHERE UPPER(TRIM(e.descripcion)) = UPPER(TRIM(:nombre))")
    boolean existsByDescripcionNombre(@Param("nombre") String nombre);
}
