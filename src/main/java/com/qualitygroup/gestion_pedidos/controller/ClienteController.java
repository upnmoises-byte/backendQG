package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@PreAuthorize(com.qualitygroup.gestion_pedidos.security.AppRoles.HAS_ANY_APP_ROLE)
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> listarActivos() {
        return clienteService.listarActivos();
    }

    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        return clienteService.guardar(cliente);
    }

    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.actualizar(id, cliente);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_ELIMINAR') or hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        clienteService.desactivar(id);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAuthority('CLIENTES_REACTIVAR') or hasRole('ADMIN')")
    public Cliente reactivar(@PathVariable Long id) {
        return clienteService.reactivar(id);
    }
}