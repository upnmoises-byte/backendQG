package com.qualitygroup.gestion_pedidos.dto;

import lombok.Data;

@Data
public class UsuarioUpdateRequest {

    private String nombre;
    private String correo;
    private String rol;
    /** Si viene vacío o null, no se cambia la contraseña. */
    private String password;
    private Boolean activo;
}
