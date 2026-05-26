package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        normalizarYValidarCliente(cliente, null);
        if (cliente.getActivo() == null) {
            cliente.setActivo(true);
        }

        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente cliente) {
        Cliente actual = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        normalizarYValidarCliente(cliente, id);

        actual.setNombre(cliente.getNombre());
        actual.setDocumento(cliente.getDocumento());
        actual.setTelefono(cliente.getTelefono());
        actual.setCorreo(cliente.getCorreo());
        actual.setDireccion(cliente.getDireccion());
        actual.setTipoCliente(cliente.getTipoCliente());
        actual.setActivo(cliente.getActivo() != null ? cliente.getActivo() : actual.getActivo());

        return clienteRepository.save(actual);
    }

    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private void normalizarYValidarCliente(Cliente cliente, Long idActual) {
        String documento = cliente.getDocumento() != null ? cliente.getDocumento().trim() : "";
        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        String tipo = cliente.getTipoCliente() != null ? cliente.getTipoCliente().trim().toUpperCase(Locale.ROOT) : "";

        if (documento.isBlank()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }
        if (!documento.matches("\\d+")) {
            throw new IllegalArgumentException("El documento solo debe contener números.");
        }
        if ("DNI".equals(tipo) && documento.length() != 8) {
            throw new IllegalArgumentException("DNI inválido. Debe contener 8 dígitos");
        }
        if ("RUC".equals(tipo) && documento.length() != 11) {
            throw new IllegalArgumentException("RUC inválido. Debe contener 11 dígitos");
        }
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre o razón social es obligatorio.");
        }

        clienteRepository.findFirstByDocumentoIgnoreCase(documento).ifPresent(existente -> {
            if (!Objects.equals(existente.getId(), idActual)) {
                throw new IllegalArgumentException("Ya existe un cliente registrado con ese documento");
            }
        });

        String nombreNormalizado = normalizarNombre(nombre);
        clienteRepository.findAll().stream()
                .filter(c -> !Objects.equals(c.getId(), idActual))
                .filter(c -> normalizarNombre(c.getNombre()).equals(nombreNormalizado))
                .findFirst()
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un cliente con este nombre o razón social.");
                });

        cliente.setDocumento(documento);
        cliente.setNombre(nombre);
        cliente.setTipoCliente(tipo.isBlank() ? cliente.getTipoCliente() : tipo);
    }

    private static String normalizarNombre(String nombre) {
        return nombre == null
                ? ""
                : nombre.trim()
                        .replaceAll("\\s+", " ")
                        .toUpperCase(Locale.ROOT);
    }
}