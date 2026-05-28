package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.model.Material;
import com.qualitygroup.gestion_pedidos.service.MaterialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    @PreAuthorize(com.qualitygroup.gestion_pedidos.security.AppRoles.HAS_ANY_APP_ROLE)
    public List<Material> listar(@RequestParam(required = false) Boolean soloActivos) {
        return materialService.listar(soloActivos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Material crear(@RequestBody Material material) {
        return materialService.crear(material);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Material actualizar(@PathVariable Long id, @RequestBody Material material) {
        return materialService.actualizar(id, material);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        materialService.eliminar(id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public Material cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean activo = Boolean.TRUE.equals(body.get("activo"));
        return materialService.cambiarEstado(id, activo);
    }
}
