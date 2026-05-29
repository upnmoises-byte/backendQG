package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.dto.UsuarioAdminResponse;
import com.qualitygroup.gestion_pedidos.dto.UsuarioCreateRequest;
import com.qualitygroup.gestion_pedidos.dto.UsuarioUpdateRequest;
import com.qualitygroup.gestion_pedidos.model.RolCatalogo;
import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.RolCatalogoRepository;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioAdminService {

    private static final String PASSWORD_DEFECTO = "123456";
    private static final Pattern ROL_PATTERN = Pattern.compile("^[A-Z0-9_]{2,64}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarioRepository;
    private final RolCatalogoRepository rolCatalogoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdminService(
            UsuarioRepository usuarioRepository,
            RolCatalogoRepository rolCatalogoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolCatalogoRepository = rolCatalogoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream()
                .map(UsuarioAdminResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UsuarioAdminResponse crear(UsuarioCreateRequest req) {
        validarRolCatalogo(req.getRol());
        if (req.getCorreo() == null || req.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (req.getNombre() == null || req.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String correo = req.getCorreo().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            throw new IllegalArgumentException("Ingrese un correo válido");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }

        Usuario u = new Usuario();
        u.setNombre(req.getNombre().trim());
        u.setCorreo(correo);
        u.setRol(req.getRol().trim().toUpperCase());
        String raw = req.getPassword() != null && !req.getPassword().isBlank()
                ? req.getPassword()
                : PASSWORD_DEFECTO;
        if (raw.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        u.setPassword(passwordEncoder.encode(raw));
        u.setActivo(req.getActivo() == null || Boolean.TRUE.equals(req.getActivo()));
        return UsuarioAdminResponse.fromEntity(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioAdminResponse actualizar(Long id, UsuarioUpdateRequest req) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (req.getNombre() != null && !req.getNombre().isBlank()) {
            u.setNombre(req.getNombre().trim());
        }
        if (req.getCorreo() != null && !req.getCorreo().isBlank()) {
            String correo = req.getCorreo().trim().toLowerCase();
            if (!EMAIL_PATTERN.matcher(correo).matches()) {
                throw new IllegalArgumentException("Ingrese un correo válido");
            }
            usuarioRepository.findByCorreo(correo).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe otro usuario con ese correo");
                }
            });
            u.setCorreo(correo);
        }
        if (req.getRol() != null && !req.getRol().isBlank()) {
            validarRolCatalogo(req.getRol());
            u.setRol(req.getRol().trim().toUpperCase());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getActivo() != null) {
            u.setActivo(req.getActivo());
        }
        return UsuarioAdminResponse.fromEntity(usuarioRepository.save(u));
    }

    public List<String> listarRolesCatalogo() {
        return rolCatalogoRepository.findAll().stream()
                .map(RolCatalogo::getNombre)
                .sorted()
                .toList();
    }

    @Transactional
    public String crearRolCatalogo(String nombreRaw) {
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
        RolCatalogo r = new RolCatalogo();
        r.setNombre(nombre);
        rolCatalogoRepository.save(r);
        return nombre;
    }

    @Transactional
    public void eliminarRolCatalogo(String nombreRaw) {
        if (nombreRaw == null || nombreRaw.isBlank()) {
            throw new IllegalArgumentException("Indique el rol a eliminar");
        }
        String nombre = nombreRaw.trim().toUpperCase();
        if (!rolCatalogoRepository.existsById(nombre)) {
            throw new IllegalArgumentException("Rol no encontrado");
        }
        long n = usuarioRepository.countByRol(nombre);
        if (n > 0) {
            throw new IllegalArgumentException("No se puede eliminar: hay " + n + " usuario(s) con ese rol");
        }
        rolCatalogoRepository.deleteById(nombre);
    }

    @Transactional
    public UsuarioAdminResponse actualizarRol(Long id, String rolRaw) {
        if (rolRaw == null || rolRaw.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        validarRolCatalogo(rolRaw);
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setRol(rolRaw.trim().toUpperCase());
        return UsuarioAdminResponse.fromEntity(usuarioRepository.save(u));
    }

    private void validarRolCatalogo(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        String key = rol.trim().toUpperCase();
        RolCatalogo catalogo = rolCatalogoRepository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El rol no existe en el catálogo. Créelo primero en «Roles del sistema»."));
        if (!Boolean.TRUE.equals(catalogo.getActivo())) {
            throw new IllegalArgumentException("El rol está desactivado");
        }
    }
}
