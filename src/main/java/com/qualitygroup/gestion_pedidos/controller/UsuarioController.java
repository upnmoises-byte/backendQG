package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.LoginRequest;
import com.qualitygroup.gestion_pedidos.dto.LoginResponse;
import com.qualitygroup.gestion_pedidos.dto.UsuarioDto;
import com.qualitygroup.gestion_pedidos.model.Usuario;
import com.qualitygroup.gestion_pedidos.security.JwtService;
import com.qualitygroup.gestion_pedidos.service.PermisoService;
import com.qualitygroup.gestion_pedidos.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final PermisoService permisoService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService,
            PermisoService permisoService
    ) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.permisoService = permisoService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        Optional<Usuario> usuarioOpt = usuarioService.authenticate(
                body.getCorreo(),
                body.getPassword()
        );

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "mensaje", "Credenciales incorrectas"
            ));
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtService.generateToken(usuario);
        var permisos = permisoService.permisosDeRol(usuario.getRol());
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .usuario(UsuarioDto.fromEntity(usuario, permisos))
                .build();

        return ResponseEntity.ok(response);
    }
}
