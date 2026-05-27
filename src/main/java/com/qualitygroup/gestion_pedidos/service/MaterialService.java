package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.Material;
import com.qualitygroup.gestion_pedidos.repository.MaterialRepository;
import com.qualitygroup.gestion_pedidos.repository.PedidoDetalleRepository;
import com.qualitygroup.gestion_pedidos.util.MaterialCatalogoParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;

    public MaterialService(
            MaterialRepository materialRepository,
            PedidoDetalleRepository pedidoDetalleRepository
    ) {
        this.materialRepository = materialRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
    }

    public List<Material> listar(Boolean soloActivos) {
        if (Boolean.TRUE.equals(soloActivos)) {
            return materialRepository.findByActivoTrueOrderByNombreAsc();
        }
        return materialRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Material crear(Material material) {
        normalizar(material);
        validarNombreUnico(material.getNombre(), null);
        if (material.getActivo() == null) {
            material.setActivo(true);
        }
        return materialRepository.save(material);
    }

    @Transactional
    public Material actualizar(Long id, Material material) {
        Material actual = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));
        normalizar(material);
        validarNombreUnico(material.getNombre(), id);

        actual.setNombre(material.getNombre());
        actual.setMarca(material.getMarca());
        actual.setColor(material.getColor());
        actual.setTipo(material.getTipo());
        actual.setEspesor(material.getEspesor());
        actual.setMedida(material.getMedida());
        if (material.getActivo() != null) {
            actual.setActivo(material.getActivo());
        }

        return materialRepository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        if (pedidoDetalleRepository.existsByMaterialNombre(material.getNombre())) {
            throw new RuntimeException(
                    "No se puede eliminar: el material está en uso en pedidos. Desactívelo en su lugar."
            );
        }

        materialRepository.delete(material);
    }

    @Transactional
    public Material cambiarEstado(Long id, boolean activo) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));
        material.setActivo(activo);
        return materialRepository.save(material);
    }

    public Material crearDesdeNombre(String nombreLinea) {
        Material parsed = MaterialCatalogoParser.fromLinea(nombreLinea);
        if (materialRepository.existsByNombreIgnoreCase(parsed.getNombre())) {
            return materialRepository.findFirstByNombreIgnoreCase(parsed.getNombre()).orElse(parsed);
        }
        return materialRepository.save(parsed);
    }

    private void normalizar(Material material) {
        if (material.getNombre() == null || material.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del material es obligatorio");
        }

        String nombre = material.getNombre().trim().toUpperCase(Locale.ROOT);
        material.setNombre(nombre);

        if (material.getMarca() != null) {
            material.setMarca(material.getMarca().trim().toUpperCase(Locale.ROOT));
        }
        if (material.getColor() != null) {
            material.setColor(material.getColor().trim().toUpperCase(Locale.ROOT));
        }
        if (material.getTipo() != null) {
            material.setTipo(material.getTipo().trim().toUpperCase(Locale.ROOT));
        } else {
            material.setTipo("NORMAL");
        }
        if (material.getEspesor() != null) {
            material.setEspesor(material.getEspesor().trim().toUpperCase(Locale.ROOT));
        }
        if (material.getMedida() != null) {
            material.setMedida(material.getMedida().trim());
        }
    }

    private void validarNombreUnico(String nombre, Long idExcluir) {
        boolean duplicado = idExcluir == null
                ? materialRepository.existsByNombreIgnoreCase(nombre)
                : materialRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idExcluir);

        if (duplicado) {
            throw new RuntimeException("Ya existe un material con el nombre: " + nombre);
        }
    }
}
