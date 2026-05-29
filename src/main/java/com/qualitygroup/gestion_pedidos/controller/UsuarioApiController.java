package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.UsuarioAdminResponse;
import com.qualitygroup.gestion_pedidos.dto.UsuarioRolRequest;
import com.qualitygroup.gestion_pedidos.service.UsuarioAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioApiController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_VER') or hasRole('ADMIN')")
    public List<UsuarioAdminResponse> listar() {
        return usuarioAdminService.listarUsuarios();
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasAuthority('ROLES_EDITAR') or hasRole('ADMIN')")
    public UsuarioAdminResponse actualizarRol(@PathVariable Long id, @RequestBody UsuarioRolRequest body) {
        String rol = body != null ? body.getRol() : null;
        return usuarioAdminService.actualizarRol(id, rol);
    }
}
