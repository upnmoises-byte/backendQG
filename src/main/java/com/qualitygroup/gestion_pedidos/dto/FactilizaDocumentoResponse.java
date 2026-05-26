package com.qualitygroup.gestion_pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactilizaDocumentoResponse {

    private boolean success;
    private String mensaje;
    private String tipoDocumento;

    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;

    private String ruc;
    private String razonSocial;
    private String estado;
    private String condicion;

    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;
}
