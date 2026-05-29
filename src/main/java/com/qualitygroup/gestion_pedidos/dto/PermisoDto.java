package com.qualitygroup.gestion_pedidos.dto;

import com.qualitygroup.gestion_pedidos.model.Permiso;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermisoDto {

    String codigo;
    String modulo;
    String descripcion;

    public static PermisoDto fromEntity(Permiso p) {
        return PermisoDto.builder()
                .codigo(p.getCodigo())
                .modulo(p.getModulo())
                .descripcion(p.getDescripcion())
                .build();
    }
}
