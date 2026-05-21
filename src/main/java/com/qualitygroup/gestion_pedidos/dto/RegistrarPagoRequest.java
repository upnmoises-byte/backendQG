package com.qualitygroup.gestion_pedidos.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegistrarPagoRequest {

    private BigDecimal monto;
    /** BCP, YAPE, BBVA, EFECTIVO, VISA */
    private String metodoPago;
    /** Obligatorio salvo EFECTIVO (número de operación / voucher). */
    private String codigoPago;
    private String nota;
}
