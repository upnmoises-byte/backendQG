package com.qualitygroup.gestion_pedidos.security;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import com.qualitygroup.gestion_pedidos.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoService permisoService;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoAndActivoTrue(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String rol = usuario.getRol() != null ? usuario.getRol().trim().toUpperCase() : "";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (!rol.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
            for (String permiso : permisoService.permisosDeRol(rol)) {
                authorities.add(new SimpleGrantedAuthority(permiso));
            }
        }

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword() != null ? usuario.getPassword() : "")
                .authorities(authorities)
                .build();
    }
}
