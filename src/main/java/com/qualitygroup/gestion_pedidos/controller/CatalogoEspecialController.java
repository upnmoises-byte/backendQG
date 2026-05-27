package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.model.CatalogoEspecial;
import com.qualitygroup.gestion_pedidos.service.CatalogoEspecialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/especiales")
public class CatalogoEspecialController {

    private final CatalogoEspecialService catalogoEspecialService;

    public CatalogoEspecialController(CatalogoEspecialService catalogoEspecialService) {
        this.catalogoEspecialService = catalogoEspecialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCCION','CAJA','VENTAS_1','VENTAS_2','VENTAS_3','VENTAS_4','VENDEDORA')")
    public List<CatalogoEspecial> listar(@RequestParam(required = false) Boolean soloActivos) {
        return catalogoEspecialService.listar(soloActivos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CatalogoEspecial crear(@RequestBody CatalogoEspecial especial) {
        return catalogoEspecialService.crear(especial);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CatalogoEspecial actualizar(@PathVariable Long id, @RequestBody CatalogoEspecial especial) {
        return catalogoEspecialService.actualizar(id, especial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        catalogoEspecialService.eliminar(id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public CatalogoEspecial cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean activo = Boolean.TRUE.equals(body.get("activo"));
        return catalogoEspecialService.cambiarEstado(id, activo);
    }
}
