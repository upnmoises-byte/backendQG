package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByActivoTrue();

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}