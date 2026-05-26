package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByActivoTrue();

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    Optional<Cliente> findFirstByDocumentoIgnoreCase(String documento);
}