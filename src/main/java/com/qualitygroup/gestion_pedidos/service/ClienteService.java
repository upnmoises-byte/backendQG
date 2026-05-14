package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarActivos() {
        return clienteRepository.findByActivoTrue();
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getActivo() == null) {
            cliente.setActivo(true);
        }

        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente cliente) {
        Cliente actual = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        actual.setNombre(cliente.getNombre());
        actual.setDocumento(cliente.getDocumento());
        actual.setTelefono(cliente.getTelefono());
        actual.setCorreo(cliente.getCorreo());
        actual.setDireccion(cliente.getDireccion());
        actual.setTipoCliente(cliente.getTipoCliente());
        actual.setActivo(cliente.getActivo());

        return clienteRepository.save(actual);
    }

    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }
}