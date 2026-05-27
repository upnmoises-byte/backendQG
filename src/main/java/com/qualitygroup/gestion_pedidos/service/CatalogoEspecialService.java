package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.CatalogoEspecial;
import com.qualitygroup.gestion_pedidos.repository.CatalogoEspecialRepository;
import com.qualitygroup.gestion_pedidos.repository.PedidoDetalleEspecialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CatalogoEspecialService {

    private final CatalogoEspecialRepository catalogoEspecialRepository;
    private final PedidoDetalleEspecialRepository pedidoDetalleEspecialRepository;

    public CatalogoEspecialService(
            CatalogoEspecialRepository catalogoEspecialRepository,
            PedidoDetalleEspecialRepository pedidoDetalleEspecialRepository
    ) {
        this.catalogoEspecialRepository = catalogoEspecialRepository;
        this.pedidoDetalleEspecialRepository = pedidoDetalleEspecialRepository;
    }

    public List<CatalogoEspecial> listar(Boolean soloActivos) {
        if (Boolean.TRUE.equals(soloActivos)) {
            return catalogoEspecialRepository.findByActivoTrueOrderByNombreAsc();
        }
        return catalogoEspecialRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public CatalogoEspecial crear(CatalogoEspecial especial) {
        normalizar(especial);
        validarNombreUnico(especial.getNombre(), null);
        if (especial.getActivo() == null) {
            especial.setActivo(true);
        }
        return catalogoEspecialRepository.save(especial);
    }

    @Transactional
    public CatalogoEspecial actualizar(Long id, CatalogoEspecial especial) {
        CatalogoEspecial actual = catalogoEspecialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especial no encontrado"));
        normalizar(especial);
        validarNombreUnico(especial.getNombre(), id);

        actual.setNombre(especial.getNombre());
        actual.setDescripcion(especial.getDescripcion());
        if (especial.getActivo() != null) {
            actual.setActivo(especial.getActivo());
        }

        return catalogoEspecialRepository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        CatalogoEspecial especial = catalogoEspecialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especial no encontrado"));

        if (pedidoDetalleEspecialRepository.existsByDescripcionNombre(especial.getNombre())) {
            throw new RuntimeException(
                    "No se puede eliminar: el especial está en uso en pedidos. Desactívelo en su lugar."
            );
        }

        catalogoEspecialRepository.delete(especial);
    }

    @Transactional
    public CatalogoEspecial cambiarEstado(Long id, boolean activo) {
        CatalogoEspecial especial = catalogoEspecialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especial no encontrado"));
        especial.setActivo(activo);
        return catalogoEspecialRepository.save(especial);
    }

    private void normalizar(CatalogoEspecial especial) {
        if (especial.getNombre() == null || especial.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del especial es obligatorio");
        }
        especial.setNombre(especial.getNombre().trim().toUpperCase(Locale.ROOT));
        if (especial.getDescripcion() != null) {
            especial.setDescripcion(especial.getDescripcion().trim());
        }
    }

    private void validarNombreUnico(String nombre, Long idExcluir) {
        boolean duplicado = idExcluir == null
                ? catalogoEspecialRepository.existsByNombreIgnoreCase(nombre)
                : catalogoEspecialRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idExcluir);

        if (duplicado) {
            throw new RuntimeException("Ya existe un especial con el nombre: " + nombre);
        }
    }
}
