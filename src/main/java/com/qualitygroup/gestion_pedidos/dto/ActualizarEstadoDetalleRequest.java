package com.qualitygroup.gestion_pedidos.dto;

import lombok.Data;

@Data
public class ActualizarEstadoDetalleRequest {

    private String estado;
    private String usuarioNombre;
    private String usuarioCorreo;
    private String usuarioRol;
}
