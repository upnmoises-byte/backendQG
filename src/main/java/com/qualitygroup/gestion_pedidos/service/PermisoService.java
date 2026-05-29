package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.dto.PermisoDto;
import com.qualitygroup.gestion_pedidos.model.Permiso;
import com.qualitygroup.gestion_pedidos.model.RolPermiso;
import com.qualitygroup.gestion_pedidos.repository.PermisoRepository;
import com.qualitygroup.gestion_pedidos.repository.RolPermisoRepository;
import com.qualitygroup.gestion_pedidos.security.PermisoCatalogo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

    public PermisoService(PermisoRepository permisoRepository, RolPermisoRepository rolPermisoRepository) {
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
    }

    @Transactional
    public void sincronizarCatalogo() {
        for (Permiso def : PermisoCatalogo.todos()) {
            if (!permisoRepository.existsById(def.getCodigo())) {
                permisoRepository.save(def);
            }
        }
    }

    @Transactional
    public void sembrarPermisosPorDefectoSiVacios(String rolNombre) {
        if (rolPermisoRepository.existsByRolNombre(rolNombre)) {
            return;
        }
        Set<String> defs = PermisoCatalogo.permisosPorDefecto().get(rolNombre);
        if (defs == null || defs.isEmpty()) {
            return;
        }
        guardarPermisosRol(rolNombre, new ArrayList<>(defs), false);
    }

    public List<PermisoDto> listarPermisos() {
        return permisoRepository.findAllByOrderByModuloAscCodigoAsc().stream()
                .map(PermisoDto::fromEntity)
                .toList();
    }

    public List<String> permisosDeRol(String rolNombre) {
        String rol = normalizarRol(rolNombre);
        if (PermisoCatalogo.ROL_ADMIN.equals(rol)) {
            return new ArrayList<>(PermisoCatalogo.todosCodigos());
        }
        return rolPermisoRepository.findByRolNombre(rol).stream()
                .map(RolPermiso::getPermisoCodigo)
                .sorted()
                .toList();
    }

    @Transactional
    public List<String> guardarPermisosRol(String rolNombre, List<String> codigos, boolean validarAdmin) {
        String rol = normalizarRol(rolNombre);
        Set<String> validos = PermisoCatalogo.todosCodigos();
        Set<String> incoming = codigos == null
                ? Set.of()
                : codigos.stream()
                        .map(c -> c == null ? "" : c.trim().toUpperCase())
                        .filter(c -> !c.isBlank() && validos.contains(c))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (PermisoCatalogo.ROL_ADMIN.equals(rol) && validarAdmin) {
            incoming.addAll(PermisoCatalogo.criticosAdmin());
        }

        rolPermisoRepository.deleteByRolNombre(rol);
        for (String codigo : incoming) {
            rolPermisoRepository.save(new RolPermiso(rol, codigo));
        }
        return permisosDeRol(rol);
    }

    public void restaurarPermisosPorDefecto(String rolNombre) {
        String rol = normalizarRol(rolNombre);
        Set<String> defs = PermisoCatalogo.permisosPorDefecto().get(rol);
        if (defs == null) {
            guardarPermisosRol(rol, List.of(), true);
            return;
        }
        guardarPermisosRol(rol, new ArrayList<>(defs), true);
    }

    private static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("Rol inválido");
        }
        return rol.trim().toUpperCase();
    }
}
