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

    private static final String[][] ROLES = {
            {"ADMIN", "Administrador del sistema"},
            {"GERENCIA", "Gerencia y supervisión"},
            {"PRODUCCION", "Área de producción"},
            {"CAJA", "Caja y cobranzas"},
            {"VENDEDORA", "Ventas y atención al cliente"}
    };

    private final RolCatalogoRepository rolCatalogoRepository;

    public RolesCatalogoBootstrap(RolCatalogoRepository rolCatalogoRepository) {
        this.rolCatalogoRepository = rolCatalogoRepository;
    }

    @Override
    public void run(String... args) {
        for (String[] par : ROLES) {
            String nombre = par[0];
            RolCatalogo r = rolCatalogoRepository.findById(nombre).orElseGet(() -> {
                RolCatalogo nuevo = new RolCatalogo();
                nuevo.setNombre(nombre);
                nuevo.setActivo(true);
                return nuevo;
            });
            if (r.getDescripcion() == null || r.getDescripcion().isBlank()) {
                r.setDescripcion(par[1]);
            }
            if (r.getActivo() == null) {
                r.setActivo(true);
            }
            rolCatalogoRepository.save(r);
        }
    }
}
