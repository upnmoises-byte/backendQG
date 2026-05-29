package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.RolDto;
import com.qualitygroup.gestion_pedidos.dto.RolEstadoRequest;
import com.qualitygroup.gestion_pedidos.dto.RolPermisosRequest;
import com.qualitygroup.gestion_pedidos.dto.RolRequest;
import com.qualitygroup.gestion_pedidos.service.PermisoService;
import com.qualitygroup.gestion_pedidos.service.RolAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolAdminService rolAdminService;
    private final PermisoService permisoService;

    public RolController(RolAdminService rolAdminService, PermisoService permisoService) {
        this.rolAdminService = rolAdminService;
        this.permisoService = permisoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_VER') or hasRole('ADMIN')")
    public List<RolDto> listar() {
        return rolAdminService.listarRoles();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES_CREAR') or hasRole('ADMIN')")
    public RolDto crear(@RequestBody RolRequest body) {
        return rolAdminService.crear(body);
    }

    @PutMapping("/{nombre}")
    @PreAuthorize("hasAuthority('ROLES_EDITAR') or hasRole('ADMIN')")
    public RolDto actualizar(@PathVariable String nombre, @RequestBody RolRequest body) {
        return rolAdminService.actualizar(nombre, body);
    }

    @DeleteMapping("/{nombre}")
    @PreAuthorize("hasAuthority('ROLES_ELIMINAR') or hasRole('ADMIN')")
    public void eliminar(@PathVariable String nombre) {
        rolAdminService.eliminar(nombre);
    }

    @PatchMapping("/{nombre}/estado")
    @PreAuthorize("hasAuthority('ROLES_EDITAR') or hasRole('ADMIN')")
    public RolDto cambiarEstado(@PathVariable String nombre, @RequestBody RolEstadoRequest body) {
        boolean activo = body != null && Boolean.TRUE.equals(body.getActivo());
        return rolAdminService.cambiarEstado(nombre, activo);
    }

    @GetMapping("/{nombre}/permisos")
    @PreAuthorize("hasAuthority('ROLES_VER') or hasRole('ADMIN')")
    public List<String> permisosDeRol(@PathVariable String nombre) {
        return permisoService.permisosDeRol(nombre);
    }

    @PutMapping("/{nombre}/permisos")
    @PreAuthorize("hasAuthority('ROLES_ASIGNAR_PERMISOS') or hasRole('ADMIN')")
    public List<String> guardarPermisos(@PathVariable String nombre, @RequestBody RolPermisosRequest body) {
        List<String> codigos = body != null ? body.getPermisos() : List.of();
        return permisoService.guardarPermisosRol(nombre, codigos, true);
    }

    @PostMapping("/{nombre}/permisos/restaurar")
    @PreAuthorize("hasAuthority('ROLES_ASIGNAR_PERMISOS') or hasRole('ADMIN')")
    public List<String> restaurarPermisos(@PathVariable String nombre) {
        permisoService.restaurarPermisosPorDefecto(nombre);
        return permisoService.permisosDeRol(nombre);
    }
}
