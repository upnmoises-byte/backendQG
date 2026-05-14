package com.qualitygroup.gestion_pedidos.dto;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioAdminResponse {

    Long id;
    String nombre;
    String correo;
    String rol;
    boolean activo;

    public static UsuarioAdminResponse fromEntity(Usuario u) {
        return UsuarioAdminResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .activo(Boolean.TRUE.equals(u.getActivo()))
                .build();
    }
}
