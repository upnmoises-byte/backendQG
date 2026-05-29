package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.RolCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolCatalogoRepository extends JpaRepository<RolCatalogo, String> {

    List<RolCatalogo> findAllByOrderByNombreAsc();

    List<RolCatalogo> findByActivoTrueOrderByNombreAsc();
}
