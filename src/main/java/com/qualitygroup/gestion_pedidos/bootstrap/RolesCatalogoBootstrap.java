package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.RolCatalogo;
import com.qualitygroup.gestion_pedidos.repository.RolCatalogoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Roles conocidos por la aplicación (JWT y matriz de navegación).
 */
@Component
@Order(1)
public class RolesCatalogoBootstrap implements CommandLineRunner {

    private static final String[] ROLES = {
            "ADMIN", "GERENCIA", "PRODUCCION", "CAJA", "VENDEDORA"
    };

    private final RolCatalogoRepository rolCatalogoRepository;

    public RolesCatalogoBootstrap(RolCatalogoRepository rolCatalogoRepository) {
        this.rolCatalogoRepository = rolCatalogoRepository;
    }

    @Override
    public void run(String... args) {
        for (String nombre : ROLES) {
            if (!rolCatalogoRepository.existsById(nombre)) {
                RolCatalogo r = new RolCatalogo();
                r.setNombre(nombre);
                rolCatalogoRepository.save(r);
            }
        }
    }
}
