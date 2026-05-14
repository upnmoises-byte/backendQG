package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.service.ConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfigService configService;

    public ConfiguracionController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public Map<String, String> obtener() {
        return configService.obtenerMapa();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> guardar(@RequestBody Map<String, String> body) {
        configService.guardarMapa(body);
        return configService.obtenerMapa();
    }
}
