package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica por correo y contraseña. Soporta hashes BCrypt y migración desde contraseña en texto plano.
     */
    public Optional<Usuario> authenticate(String correo, String rawPassword) {
        if (correo == null || correo.isBlank() || rawPassword == null) {
            return Optional.empty();
        }

        Optional<Usuario> opt = usuarioRepository.findByCorreoAndActivoTrue(correo.trim());
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = opt.get();
        String stored = usuario.getPassword();
        if (stored == null || stored.isBlank()) {
            return Optional.empty();
        }

        if (passwordEncoder.matches(rawPassword, stored)) {
            return Optional.of(usuario);
        }

        if (stored.equals(rawPassword)) {
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuario);
            return Optional.of(usuario);
        }

        return Optional.empty();
    }
}
