package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.ReportePedidosFiltro;
import com.qualitygroup.gestion_pedidos.service.ReportePedidosPdfService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('ADMIN','PRODUCCION','CAJA','VENTAS_1','VENTAS_2','VENTAS_3','VENTAS_4','VENDEDORA')")
public class ReporteController {

    private static final Map<String, String> VENDEDORA_POR_ROL = Map.of(
            "VENTAS_1", "ISAMAR",
            "VENTAS_2", "ANABEL",
            "VENTAS_3", "DIANA",
            "VENTAS_4", "MELISSA"
    );

    private final ReportePedidosPdfService reportePedidosPdfService;

    public ReporteController(ReportePedidosPdfService reportePedidosPdfService) {
        this.reportePedidosPdfService = reportePedidosPdfService;
    }

    @GetMapping(value = "/pedidos.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> pedidosPdf(
            Authentication authentication,
            @RequestParam(value = "cliente", required = false) String cliente,
            @RequestParam(value = "vendedora", required = false) String vendedora,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "maquina", required = false) String maquina,
            @RequestParam(value = "saldo", required = false) String saldo,
            @RequestParam(value = "fiDesde", required = false) LocalDate fiDesde,
            @RequestParam(value = "fiHasta", required = false) LocalDate fiHasta,
            @RequestParam(value = "feDesde", required = false) LocalDate feDesde,
            @RequestParam(value = "feHasta", required = false) LocalDate feHasta
    ) {
        ReportePedidosFiltro filtro = new ReportePedidosFiltro(
                cliente,
                vendedora,
                estado,
                maquina,
                saldo,
                fiDesde,
                fiHasta,
                feDesde,
                feHasta
        );
        Optional<String> vFija = vendedoraFijaPorRol(authentication);
        byte[] pdf = reportePedidosPdfService.generarPedidosPdf(filtro, vFija);
        ByteArrayResource body = new ByteArrayResource(pdf);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reporte-pedidos.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(body);
    }

    private static Optional<String> vendedoraFijaPorRol(Authentication auth) {
        if (auth == null) {
            return Optional.empty();
        }
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga == null || ga.getAuthority() == null) {
                continue;
            }
            String r = ga.getAuthority().replace("ROLE_", "").trim().toUpperCase();
            String v = VENDEDORA_POR_ROL.get(r);
            if (v != null) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }
}
