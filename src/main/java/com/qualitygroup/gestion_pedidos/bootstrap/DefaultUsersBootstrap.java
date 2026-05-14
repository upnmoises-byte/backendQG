package com.qualitygroup.gestion_pedidos.bootstrap;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea usuarios por defecto si no existe aún un registro con ese correo.
 * Así, si la BD ya tenía solo el admin, al reiniciar la app se completan producción, caja y ventas.
 */
@Component
@Order(2)
public class DefaultUsersBootstrap implements CommandLineRunner {

    private static final String PASSWORD_INICIAL = "123456";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUsersBootstrap(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("Administrador", "admin@qg.com", PASSWORD_INICIAL, "ADMIN");
        crearSiNoExiste("Producción", "produccion@qg.com", PASSWORD_INICIAL, "PRODUCCION");
        crearSiNoExiste("Caja", "caja@qg.com", PASSWORD_INICIAL, "CAJA");
        crearSiNoExiste("Ventas 1 — Isamar", "isamar@qg.com", PASSWORD_INICIAL, "VENTAS_1");
        crearSiNoExiste("Ventas 2 — Anabel", "anabel@qg.com", PASSWORD_INICIAL, "VENTAS_2");
        crearSiNoExiste("Ventas 3 — Diana", "diana@qg.com", PASSWORD_INICIAL, "VENTAS_3");
        crearSiNoExiste("Ventas 4 — Melissa", "melissa@qg.com", PASSWORD_INICIAL, "VENTAS_4");
    }

    private void crearSiNoExiste(String nombre, String correo, String passwordPlano, String rol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }
        usuarioRepository.save(usuario(nombre, correo, passwordPlano, rol));
    }

    private Usuario usuario(String nombre, String correo, String passwordPlano, String rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(passwordPlano));
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }
}
