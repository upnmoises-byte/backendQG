package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByActivoTrue();

    List<Cliente> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    Optional<Cliente> findByDocumentoIgnoreCase(String documento);

    Optional<Cliente> findByDocumentoIgnoreCaseAndActivoTrue(String documento);

    boolean existsByDocumentoIgnoreCaseAndActivoTrue(String documento);

    boolean existsByDocumentoIgnoreCaseAndActivoTrueAndIdNot(String documento, Long id);
}
