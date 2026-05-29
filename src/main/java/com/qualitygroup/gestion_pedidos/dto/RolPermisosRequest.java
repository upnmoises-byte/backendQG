package com.qualitygroup.gestion_pedidos.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolPermisosRequest {

    private List<String> permisos;
}
