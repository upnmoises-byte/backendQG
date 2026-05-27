package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.CatalogoEspecial;
import com.qualitygroup.gestion_pedidos.repository.CatalogoEspecialRepository;
import com.qualitygroup.gestion_pedidos.service.CatalogoEspecialService;
import com.qualitygroup.gestion_pedidos.service.MaterialService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(20)
public class CatalogoProduccionBootstrap implements CommandLineRunner {

    private static final String[] ESPECIALES_INICIALES = {
            "PUNTAS BOLEADAS",
            "CORTES L",
            "CORTE ESPECIAL DEL CLIENTE",
            "REGROSADO CON TORNILLO",
            "REGROSADO CON PEGAMENTO",
            "CHAFLANES",
            "CORTE 45°"
    };

    private final MaterialService materialService;
    private final CatalogoEspecialService catalogoEspecialService;
    private final CatalogoEspecialRepository catalogoEspecialRepository;

    public CatalogoProduccionBootstrap(
            MaterialService materialService,
            CatalogoEspecialService catalogoEspecialService,
            CatalogoEspecialRepository catalogoEspecialRepository
    ) {
        this.materialService = materialService;
        this.catalogoEspecialService = catalogoEspecialService;
        this.catalogoEspecialRepository = catalogoEspecialRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedMateriales();
        seedEspeciales();
    }

    private void seedMateriales() throws Exception {
        ClassPathResource resource = new ClassPathResource("catalogo/materiales-iniciales.txt");
        if (!resource.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String nombre = line.trim();
                if (nombre.isEmpty() || nombre.startsWith("#")) {
                    continue;
                }
                materialService.crearDesdeNombre(nombre);
            }
        }
    }

    private void seedEspeciales() {
        for (String nombre : ESPECIALES_INICIALES) {
            if (catalogoEspecialRepository.existsByNombreIgnoreCase(nombre)) {
                continue;
            }
            CatalogoEspecial especial = new CatalogoEspecial();
            especial.setNombre(nombre);
            especial.setDescripcion(nombre);
            especial.setActivo(true);
            catalogoEspecialService.crear(especial);
        }
    }
}
