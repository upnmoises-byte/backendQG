package com.qualitygroup.gestion_pedidos.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qualitygroup.gestion_pedidos.dto.ReportePedidosFiltro;
import com.qualitygroup.gestion_pedidos.model.Cliente;
import com.qualitygroup.gestion_pedidos.model.Pedido;
import com.qualitygroup.gestion_pedidos.model.PedidoDetalle;
import com.qualitygroup.gestion_pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReportePedidosPdfService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final PedidoRepository pedidoRepository;

    public ReportePedidosPdfService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarPedidosPdf(ReportePedidosFiltro filtro, Optional<String> vendedoraImpuestaPorRol) {
        List<Pedido> ordenados = pedidoRepository.findAllWithDetalles().stream()
                .sorted(Comparator.comparing(p -> p.getPrioridad() == null ? 0 : p.getPrioridad()))
                .toList();

        List<Pedido> base = vendedoraImpuestaPorRol
                .map(v -> ordenados.stream().filter(p -> v.equals(p.getVendedora())).toList())
                .orElse(ordenados);

        List<Pedido> filtrados = base.stream().filter(p -> coincideFiltros(p, filtro)).toList();

        return construirPdf(filtrados);
    }

    private boolean coincideFiltros(Pedido p, ReportePedidosFiltro f) {
        String qCliente = f.cliente() != null ? f.cliente().trim().toLowerCase(Locale.ROOT) : "";
        if (!qCliente.isEmpty()) {
            String nombre = Optional.ofNullable(p.getCliente()).map(Cliente::getNombre).orElse("");
            if (!nombre.toLowerCase(Locale.ROOT).contains(qCliente)) {
                return false;
            }
        }
        if (f.vendedora() != null && !f.vendedora().isBlank() && !f.vendedora().equals(p.getVendedora())) {
            return false;
        }
        if (f.estado() != null && !f.estado().isBlank() && !f.estado().equals(p.getEstado())) {
            return false;
        }
        if (f.maquina() != null && !f.maquina().isBlank()) {
            boolean enCab = f.maquina().equals(p.getMaquina());
            boolean enDet = p.getDetalles() != null && p.getDetalles().stream()
                    .map(PedidoDetalle::getMaquina)
                    .anyMatch(m -> f.maquina().equals(m));
            if (!enCab && !enDet) {
                return false;
            }
        }
        BigDecimal saldo = saldoPedido(p);
        if ("DEBE".equalsIgnoreCase(f.saldo())) {
            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        } else if ("CANCELADO".equalsIgnoreCase(f.saldo())) {
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                return false;
            }
        }
        if (!fechaEnRango(p.getFechaIngreso(), f.fiDesde(), f.fiHasta())) {
            return false;
        }
        if (!fechaEnRango(p.getFechaEntrega(), f.feDesde(), f.feHasta())) {
            return false;
        }
        return true;
    }

    private static BigDecimal saldoPedido(Pedido p) {
        BigDecimal total = p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO;
        BigDecimal adel = p.getAdelanto() != null ? p.getAdelanto() : BigDecimal.ZERO;
        BigDecimal s = total.subtract(adel);
        return s.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : s;
    }

    private static boolean fechaEnRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return true;
        }
        if (fecha == null) {
            return false;
        }
        if (desde != null && fecha.isBefore(desde)) {
            return false;
        }
        return hasta == null || !fecha.isAfter(hasta);
    }

    private byte[] construirPdf(List<Pedido> filtrados) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);

        Document document = new Document(PageSize.A4.rotate(), 28, 28, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Q & G Quality Group — Reporte de pedidos", titleFont));
            document.add(new Paragraph(
                    "Generado: " + LocalDate.now().format(DF) + " " + LocalTime.now().format(TF),
                    small
            ));
            document.add(new Paragraph("Registros: " + filtrados.size(), small));
            document.add(new Paragraph(" ", small));

            float[] widths = {0.35f, 0.55f, 0.55f, 1.4f, 0.45f, 0.9f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.45f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            addHeader(table, "#", smallBold);
            addHeader(table, "Estado", smallBold);
            addHeader(table, "N° orden", smallBold);
            addHeader(table, "Cliente", smallBold);
            addHeader(table, "Cant.", smallBold);
            addHeader(table, "Color", smallBold);
            addHeader(table, "Vend.", smallBold);
            addHeader(table, "Total S/", smallBold);
            addHeader(table, "Adel. S/", smallBold);
            addHeader(table, "Saldo S/", smallBold);
            addHeader(table, "Ing.", smallBold);
            addHeader(table, "Entr.", smallBold);
            addHeader(table, "H.entr.", smallBold);

            int n = 0;
            for (Pedido p : filtrados) {
                n++;
                addCell(table, String.valueOf(n), small);
                addCell(table, nullToDash(p.getEstado()), small);
                addCell(table, nullToDash(p.getNumeroOrden()), small);
                addCell(table, Optional.ofNullable(p.getCliente()).map(Cliente::getNombre).orElse("-"), small);
                addCell(table, p.getCantidad() != null ? p.getCantidad().toPlainString() : "-", small);
                addCell(table, coloresResumen(p), small);
                addCell(table, nullToDash(p.getVendedora()), small);
                addCell(table, fmtMoney(p.getTotal()), small);
                addCell(table, fmtMoney(p.getAdelanto()), small);
                addCell(table, fmtMoney(saldoPedido(p)), small);
                addCell(table, p.getFechaIngreso() != null ? p.getFechaIngreso().format(DF) : "-", small);
                addCell(table, p.getFechaEntrega() != null ? p.getFechaEntrega().format(DF) : "-", small);
                addCell(table, p.getHoraEntrega() != null ? p.getHoraEntrega().format(TF) : "-", small);
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Error al generar PDF", e);
        }
        return out.toByteArray();
    }

    private static void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(3);
        table.addCell(c);
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setPadding(2);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(c);
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private static String coloresResumen(Pedido p) {
        StringBuilder sb = new StringBuilder();
        if (p.getColorPrincipal() != null && !p.getColorPrincipal().isBlank()) {
            sb.append(p.getColorPrincipal());
        }
        if (p.getColorSecundario() != null && !p.getColorSecundario().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(p.getColorSecundario());
        }
        if (p.getColorTercero() != null && !p.getColorTercero().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(p.getColorTercero());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String fmtMoney(BigDecimal b) {
        if (b == null) {
            return "0.00";
        }
        return String.format(Locale.US, "%.2f", b);
    }
}
