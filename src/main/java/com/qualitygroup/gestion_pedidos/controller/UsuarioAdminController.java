package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.RolNombreRequest;
import com.qualitygroup.gestion_pedidos.dto.UsuarioAdminResponse;
import com.qualitygroup.gestion_pedidos.dto.UsuarioCreateRequest;
import com.qualitygroup.gestion_pedidos.dto.UsuarioUpdateRequest;
import com.qualitygroup.gestion_pedidos.service.UsuarioAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioAdminService.listarUsuarios();
    }

    @PostMapping("/usuarios")
    public UsuarioAdminResponse crearUsuario(@RequestBody UsuarioCreateRequest body) {
        return usuarioAdminService.crear(body);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioAdminResponse actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateRequest body) {
        return usuarioAdminService.actualizar(id, body);
    }

    @GetMapping("/roles-catalogo")
    public List<String> listarRolesCatalogo() {
        return usuarioAdminService.listarRolesCatalogo();
    }

    @PostMapping("/roles-catalogo")
    public Map<String, String> crearRol(@RequestBody RolNombreRequest body) {
        String nombre = usuarioAdminService.crearRolCatalogo(body != null ? body.getNombre() : null);
        return Map.of("nombre", nombre);
    }

    @DeleteMapping("/roles-catalogo/{nombre}")
    public void eliminarRol(@PathVariable String nombre) {
        usuarioAdminService.eliminarRolCatalogo(nombre);
    }
}
