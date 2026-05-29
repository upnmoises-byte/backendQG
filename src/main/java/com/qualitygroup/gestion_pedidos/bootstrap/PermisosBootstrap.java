package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.RolCatalogo;
import com.qualitygroup.gestion_pedidos.repository.RolCatalogoRepository;
import com.qualitygroup.gestion_pedidos.service.PermisoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class PermisosBootstrap implements CommandLineRunner {

    private final PermisoService permisoService;
    private final RolCatalogoRepository rolCatalogoRepository;

    public PermisosBootstrap(PermisoService permisoService, RolCatalogoRepository rolCatalogoRepository) {
        this.permisoService = permisoService;
        this.rolCatalogoRepository = rolCatalogoRepository;
    }

    @Override
    public void run(String... args) {
        permisoService.sincronizarCatalogo();
        for (RolCatalogo rol : rolCatalogoRepository.findAll()) {
            permisoService.sembrarPermisosPorDefectoSiVacios(rol.getNombre());
        }
    }
}
