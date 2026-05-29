package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermiso.RolPermisoId> {

    List<RolPermiso> findByRolNombre(String rolNombre);

    void deleteByRolNombre(String rolNombre);

    boolean existsByRolNombre(String rolNombre);
}
