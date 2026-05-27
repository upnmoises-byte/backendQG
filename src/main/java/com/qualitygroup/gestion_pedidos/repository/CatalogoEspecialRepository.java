package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.CatalogoEspecial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogoEspecialRepository extends JpaRepository<CatalogoEspecial, Long> {

    List<CatalogoEspecial> findByActivoTrueOrderByNombreAsc();

    List<CatalogoEspecial> findAllByOrderByNombreAsc();

    Optional<CatalogoEspecial> findFirstByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
