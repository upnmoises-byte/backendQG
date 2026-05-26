package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.FactilizaDocumentoResponse;
import com.qualitygroup.gestion_pedidos.service.FactilizaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/factiliza")
@PreAuthorize("hasAnyRole('ADMIN','PRODUCCION','CAJA','VENTAS_1','VENTAS_2','VENTAS_3','VENTAS_4','VENDEDORA')")
public class FactilizaController {

    private final FactilizaService factilizaService;

    public FactilizaController(FactilizaService factilizaService) {
        this.factilizaService = factilizaService;
    }

    @GetMapping("/documento/{numero}")
    public FactilizaDocumentoResponse consultarDocumento(@PathVariable String numero) {
        return factilizaService.consultarDocumento(numero);
    }
}
