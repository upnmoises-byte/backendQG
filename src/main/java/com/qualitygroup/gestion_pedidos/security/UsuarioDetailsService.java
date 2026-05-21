package com.qualitygroup.gestion_pedidos.security;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoAndActivoTrue(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String rol = usuario.getRol() != null ? usuario.getRol().trim().toUpperCase() : "";
        String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword() != null ? usuario.getPassword() : "")
                .authorities(authority)
                .build();
    }
}
