package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.dto.RolDto;
import com.qualitygroup.gestion_pedidos.dto.RolRequest;
import com.qualitygroup.gestion_pedidos.model.RolCatalogo;
import com.qualitygroup.gestion_pedidos.repository.RolCatalogoRepository;
import com.qualitygroup.gestion_pedidos.repository.RolPermisoRepository;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import com.qualitygroup.gestion_pedidos.security.PermisoCatalogo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class RolAdminService {

    private static final Pattern ROL_PATTERN = Pattern.compile("^[A-Z0-9_]{2,64}$");

    private final RolCatalogoRepository rolCatalogoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoService permisoService;

    public RolAdminService(
            RolCatalogoRepository rolCatalogoRepository,
            RolPermisoRepository rolPermisoRepository,
            UsuarioRepository usuarioRepository,
            PermisoService permisoService
    ) {
        this.rolCatalogoRepository = rolCatalogoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.permisoService = permisoService;
    }

    public List<RolDto> listarRoles() {
        return rolCatalogoRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RolDto crear(RolRequest req) {
        String nombre = validarNombreNuevo(req != null ? req.getNombre() : null);
        RolCatalogo r = new RolCatalogo();
        r.setNombre(nombre);
        r.setDescripcion(req != null && req.getDescripcion() != null ? req.getDescripcion().trim() : null);
        r.setActivo(true);
        rolCatalogoRepository.save(r);
        permisoService.sembrarPermisosPorDefectoSiVacios(nombre);
        return toDto(r);
    }

    @Transactional
    public RolDto actualizar(String nombreRaw, RolRequest req) {
        String nombre = normalizarExistente(nombreRaw);
        RolCatalogo r = rolCatalogoRepository.findById(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        if (req != null && req.getDescripcion() != null) {
            r.setDescripcion(req.getDescripcion().trim());
        }
        if (req != null && req.getNombre() != null && !req.getNombre().isBlank()
                && !req.getNombre().trim().equalsIgnoreCase(nombre)) {
            throw new IllegalArgumentException("No se puede renombrar un rol existente");
        }
        return toDto(rolCatalogoRepository.save(r));
    }

    @Transactional
    public void eliminar(String nombreRaw) {
        String nombre = normalizarExistente(nombreRaw);
        if (PermisoCatalogo.ROL_ADMIN.equals(nombre)) {
            throw new IllegalArgumentException("No se puede eliminar el rol ADMIN");
        }
        long usuarios = usuarioRepository.countByRol(nombre);
        if (usuarios > 0) {
            throw new IllegalArgumentException("No se puede eliminar: hay " + usuarios + " usuario(s) con ese rol");
        }
        rolPermisoRepository.deleteByRolNombre(nombre);
        rolCatalogoRepository.deleteById(nombre);
    }

    @Transactional
    public RolDto cambiarEstado(String nombreRaw, boolean activo) {
        String nombre = normalizarExistente(nombreRaw);
        if (PermisoCatalogo.ROL_ADMIN.equals(nombre) && !activo) {
            throw new IllegalArgumentException("No se puede desactivar el rol ADMIN");
        }
        RolCatalogo r = rolCatalogoRepository.findById(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        r.setActivo(activo);
        return toDto(rolCatalogoRepository.save(r));
    }

    private RolDto toDto(RolCatalogo r) {
        long usuarios = usuarioRepository.countByRol(r.getNombre());
        return RolDto.builder()
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .activo(Boolean.TRUE.equals(r.getActivo()))
                .usuariosCount(usuarios)
                .build();
    }

    private String validarNombreNuevo(String nombreRaw) {
        if (nombreRaw == null || nombreRaw.isBlank()) {
            throw new IllegalArgumentException("Indique el nombre del rol");
        }
        String nombre = nombreRaw.trim().toUpperCase();
        if (!ROL_PATTERN.matcher(nombre).matches()) {
            throw new IllegalArgumentException("El rol debe ser MAYÚSCULAS, números o guión bajo (2–64 caracteres)");
        }
        if (rolCatalogoRepository.existsById(nombre)) {
            throw new IllegalArgumentException("Ese rol ya existe");
        }
        return nombre;
    }

    private String normalizarExistente(String nombreRaw) {
        if (nombreRaw == null || nombreRaw.isBlank()) {
            throw new IllegalArgumentException("Indique el rol");
        }
        String nombre = nombreRaw.trim().toUpperCase();
        if (!rolCatalogoRepository.existsById(nombre)) {
            throw new IllegalArgumentException("Rol no encontrado");
        }
        return nombre;
    }
}
