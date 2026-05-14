package com.qualitygroup.gestion_pedidos.dto;

import com.qualitygroup.gestion_pedidos.model.Usuario;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioDto {

    Long id;
    String nombre;
    String correo;
    String rol;

    public static UsuarioDto fromEntity(Usuario u) {
        return UsuarioDto.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .build();
    }
}
