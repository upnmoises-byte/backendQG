package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Migra roles legacy (VENTAS_1..4) al rol único VENDEDORA.
 */
@Component
@Order(3)
public class RolesMigracionBootstrap implements CommandLineRunner {

    private static final Map<String, String> ROL_LEGACY_A_NUEVO = Map.of(
            "VENTAS_1", "VENDEDORA",
            "VENTAS_2", "VENDEDORA",
            "VENTAS_3", "VENDEDORA",
            "VENTAS_4", "VENDEDORA"
    );

    private static final Set<String> ROLES_OBSOLETOS_CATALOGO = Set.of(
            "VENTAS_1", "VENTAS_2", "VENTAS_3", "VENTAS_4"
    );

    private final UsuarioRepository usuarioRepository;

    public RolesMigracionBootstrap(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Usuario usuario : usuarioRepository.findAll()) {
            String rolActual = normalizar(usuario.getRol());
            String rolNuevo = ROL_LEGACY_A_NUEVO.get(rolActual);
            if (rolNuevo != null && !rolNuevo.equals(rolActual)) {
                usuario.setRol(rolNuevo);
                usuarioRepository.save(usuario);
            }
        }
    }

    public static boolean esRolObsoletoCatalogo(String rol) {
        return rol != null && ROLES_OBSOLETOS_CATALOGO.contains(rol.toUpperCase(Locale.ROOT));
    }

    private static String normalizar(String rol) {
        return rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
    }
}
