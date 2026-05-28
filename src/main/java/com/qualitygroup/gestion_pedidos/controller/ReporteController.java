package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.dto.ReportePedidosFiltro;
import com.qualitygroup.gestion_pedidos.repository.UsuarioRepository;
import com.qualitygroup.gestion_pedidos.security.AppRoles;
import com.qualitygroup.gestion_pedidos.service.ReportePedidosPdfService;
import com.qualitygroup.gestion_pedidos.util.VendedoraUtil;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize(AppRoles.HAS_ANY_APP_ROLE)
public class ReporteController {

    private final ReportePedidosPdfService reportePedidosPdfService;
    private final UsuarioRepository usuarioRepository;

    public ReporteController(
            ReportePedidosPdfService reportePedidosPdfService,
            UsuarioRepository usuarioRepository
    ) {
        this.reportePedidosPdfService = reportePedidosPdfService;
        this.usuarioRepository = usuarioRepository;
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

    private Optional<String> vendedoraFijaPorRol(Authentication auth) {
        if (auth == null) {
            return Optional.empty();
        }
        boolean esVendedora = false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga == null || ga.getAuthority() == null) {
                continue;
            }
            String r = ga.getAuthority().replace("ROLE_", "").trim().toUpperCase();
            if ("VENDEDORA".equals(r)) {
                esVendedora = true;
                break;
            }
        }
        if (!esVendedora) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoAndActivoTrue(auth.getName())
                .flatMap(u -> VendedoraUtil.inferirCodigoDesdeNombre(u.getNombre()));
    }
}
