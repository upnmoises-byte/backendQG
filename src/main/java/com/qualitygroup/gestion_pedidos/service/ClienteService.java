package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarActivos() {
        return clienteRepository.findByActivoTrue();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        normalizarCliente(cliente);
        validarCamposCliente(cliente);

        String documento = cliente.getDocumento();
        Optional<Cliente> existente = clienteRepository.findByDocumentoIgnoreCase(documento);

        if (existente.isPresent()) {
            Cliente previo = existente.get();
            if (Boolean.TRUE.equals(previo.getActivo())) {
                throw new IllegalArgumentException("Ya existe un cliente activo con este documento");
            }
            aplicarDatosCliente(previo, cliente);
            previo.setActivo(true);
            return clienteRepository.save(previo);
        }

        validarNombreUnicoActivo(cliente.getNombre(), null);
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente cliente) {
        Cliente actual = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        normalizarCliente(cliente);
        validarCamposCliente(cliente);

        if (clienteRepository.existsByDocumentoIgnoreCaseAndActivoTrueAndIdNot(cliente.getDocumento(), id)) {
            throw new IllegalArgumentException("Ya existe un cliente activo con este documento");
        }

        validarNombreUnicoActivo(cliente.getNombre(), id);

        aplicarDatosCliente(actual, cliente);
        if (cliente.getActivo() != null) {
            actual.setActivo(cliente.getActivo());
        }

        return clienteRepository.save(actual);
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente reactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (Boolean.TRUE.equals(cliente.getActivo())) {
            return cliente;
        }
        if (clienteRepository.existsByDocumentoIgnoreCaseAndActivoTrue(cliente.getDocumento())) {
            throw new IllegalArgumentException("Ya existe un cliente activo con este documento");
        }
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    private void aplicarDatosCliente(Cliente destino, Cliente origen) {
        destino.setNombre(origen.getNombre());
        destino.setDocumento(origen.getDocumento());
        destino.setTelefono(origen.getTelefono());
        destino.setCorreo(origen.getCorreo());
        destino.setDireccion(origen.getDireccion());
        destino.setTipoCliente(origen.getTipoCliente());
    }

    private void normalizarCliente(Cliente cliente) {
        String documento = cliente.getDocumento() != null ? cliente.getDocumento().trim() : "";
        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        String tipo = cliente.getTipoCliente() != null ? cliente.getTipoCliente().trim().toUpperCase(Locale.ROOT) : "";

        cliente.setDocumento(documento);
        cliente.setNombre(normalizarNombre(nombre));
        cliente.setTipoCliente(tipo.isBlank() ? cliente.getTipoCliente() : tipo);
        if (cliente.getTelefono() != null) {
            cliente.setTelefono(cliente.getTelefono().trim());
        }
        if (cliente.getCorreo() != null) {
            cliente.setCorreo(cliente.getCorreo().trim().toLowerCase(Locale.ROOT));
        }
        if (cliente.getDireccion() != null) {
            cliente.setDireccion(cliente.getDireccion().trim());
        }
    }

    private void validarCamposCliente(Cliente cliente) {
        if (cliente.getDocumento() == null || cliente.getDocumento().isBlank()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio.");
        }
        if (!cliente.getDocumento().matches("\\d+")) {
            throw new IllegalArgumentException("El documento solo debe contener números.");
        }
        String tipo = cliente.getTipoCliente() != null ? cliente.getTipoCliente().toUpperCase(Locale.ROOT) : "";
        if ("DNI".equals(tipo) && cliente.getDocumento().length() != 8) {
            throw new IllegalArgumentException("DNI inválido. Debe contener 8 dígitos");
        }
        if ("RUC".equals(tipo) && cliente.getDocumento().length() != 11) {
            throw new IllegalArgumentException("RUC inválido. Debe contener 11 dígitos");
        }
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre o razón social es obligatorio.");
        }
    }

    private void validarNombreUnicoActivo(String nombre, Long idExcluir) {
        String nombreNormalizado = normalizarNombre(nombre);
        clienteRepository.findByActivoTrue().stream()
                .filter(c -> !Objects.equals(c.getId(), idExcluir))
                .filter(c -> normalizarNombre(c.getNombre()).equals(nombreNormalizado))
                .findFirst()
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un cliente activo con este nombre o razón social.");
                });
    }

    private static String normalizarNombre(String nombre) {
        return nombre == null
                ? ""
                : nombre.trim()
                        .replaceAll("\\s+", " ")
                        .toUpperCase(Locale.ROOT);
    }
}
