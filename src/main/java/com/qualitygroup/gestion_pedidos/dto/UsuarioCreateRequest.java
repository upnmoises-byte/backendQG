package com.qualitygroup.gestion_pedidos.dto;

import lombok.Data;

@Data
public class UsuarioCreateRequest {

    private String nombre;
    private String correo;
    private String rol;
    /** Si viene vacío se usa la contraseña inicial por defecto del sistema. */
    private String password;
    private Boolean activo;
}
