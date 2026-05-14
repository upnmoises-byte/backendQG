package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoAndActivoTrue(String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findAllByOrderByNombreAsc();

    long countByRol(String rol);
}