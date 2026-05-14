package com.qualitygroup.gestion_pedidos.dto;

import java.time.LocalDate;

/**
 * Parámetros de filtro del reporte PDF (alineados con la vista Reportes del frontend).
 */
public record ReportePedidosFiltro(
        String cliente,
        String vendedora,
        String estado,
        String maquina,
        String saldo,
        LocalDate fiDesde,
        LocalDate fiHasta,
        LocalDate feDesde,
        LocalDate feHasta
) {
}
