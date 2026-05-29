package com.qualitygroup.gestion_pedidos.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RolDto {

    String nombre;
    String descripcion;
    boolean activo;
    long usuariosCount;
}
