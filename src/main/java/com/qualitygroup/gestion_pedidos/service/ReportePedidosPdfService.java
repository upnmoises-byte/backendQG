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
import java.awt.Color;
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
                .sorted(this::compararPedidosReporte)
                .toList();

        List<Pedido> base = vendedoraImpuestaPorRol
                .map(v -> ordenados.stream().filter(p -> v.equals(p.getVendedora())).toList())
                .orElse(ordenados);

        List<Pedido> filtrados = base.stream().filter(p -> coincideFiltros(p, filtro)).toList();

        return construirPdf(filtrados, filtro);
    }

    private int compararPedidosReporte(Pedido a, Pedido b) {
        int pa = a.getPrioridad() == null ? 0 : a.getPrioridad();
        int pb = b.getPrioridad() == null ? 0 : b.getPrioridad();
        if (pa != pb) {
            return Integer.compare(pb, pa);
        }
        int fecha = Comparator
                .comparing((Pedido p) -> p.getFechaIngreso() != null ? p.getFechaIngreso() : LocalDate.of(1900, 1, 1))
                .reversed()
                .compare(a, b);
        if (fecha != 0) {
            return fecha;
        }
        return Comparator
                .comparing((Pedido p) -> p.getHoraIngreso() != null ? p.getHoraIngreso() : LocalTime.MIN)
                .reversed()
                .compare(a, b);
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
        if (f.estado() != null && !f.estado().isBlank()) {
            boolean enPedido = f.estado().equals(p.getEstado());
            boolean enDetalle = p.getDetalles() != null && p.getDetalles().stream()
                    .map(PedidoDetalle::getEstado)
                    .anyMatch(e -> f.estado().equals(e));
            if (!enPedido && !enDetalle) {
                return false;
            }
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

    private byte[] construirPdf(List<Pedido> filtrados, ReportePedidosFiltro filtro) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(71, 85, 105));
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42));
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));
        Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(15, 23, 42));

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            PdfPTable header = new PdfPTable(new float[]{0.16f, 1.2f, 0.55f});
            header.setWidthPercentage(100);
            PdfPCell logo = new PdfPCell(new Phrase("QG", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42))));
            logo.setHorizontalAlignment(Element.ALIGN_CENTER);
            logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logo.setBackgroundColor(new Color(250, 204, 21));
            logo.setPadding(10);
            logo.setBorderColor(new Color(226, 232, 240));
            header.addCell(logo);

            PdfPCell title = new PdfPCell();
            title.addElement(new Paragraph("Quality Group", titleFont));
            title.addElement(new Paragraph("Reporte de pedidos de produccion", subtitleFont));
            title.setBorderColor(new Color(226, 232, 240));
            title.setPadding(8);
            header.addCell(title);

            PdfPCell generated = new PdfPCell();
            generated.addElement(new Paragraph("Generado", sectionFont));
            generated.addElement(new Paragraph(LocalDate.now().format(DF) + " " + LocalTime.now().format(TF), subtitleFont));
            generated.setBorderColor(new Color(226, 232, 240));
            generated.setPadding(8);
            header.addCell(generated);
            document.add(header);

            document.add(new Paragraph(" ", small));
            document.add(new Paragraph("Filtros aplicados: " + filtrosAplicados(filtro), subtitleFont));
            document.add(new Paragraph("Registros: " + filtrados.size(), subtitleFont));
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
                addCell(table, String.valueOf(n), small, Element.ALIGN_CENTER);
                addCell(table, nullToDash(p.getEstado()), small, Element.ALIGN_CENTER);
                addCell(table, nullToDash(p.getNumeroOrden()), small, Element.ALIGN_CENTER);
                addCell(table, Optional.ofNullable(p.getCliente()).map(Cliente::getNombre).orElse("-"), small);
                addCell(table, p.getCantidad() != null ? p.getCantidad().toPlainString() : "-", small, Element.ALIGN_RIGHT);
                addCell(table, coloresResumen(p), small);
                addCell(table, nullToDash(p.getVendedora()), small, Element.ALIGN_CENTER);
                addCell(table, fmtMoney(p.getTotal()), small, Element.ALIGN_RIGHT);
                addCell(table, fmtMoney(p.getAdelanto()), small, Element.ALIGN_RIGHT);
                addCell(table, fmtMoney(saldoPedido(p)), small, Element.ALIGN_RIGHT);
                addCell(table, p.getFechaIngreso() != null ? p.getFechaIngreso().format(DF) : "-", small, Element.ALIGN_CENTER);
                addCell(table, p.getFechaEntrega() != null ? p.getFechaEntrega().format(DF) : "-", small, Element.ALIGN_CENTER);
                addCell(table, p.getHoraEntrega() != null ? p.getHoraEntrega().format(TF) : "-", small, Element.ALIGN_CENTER);
            }

            document.add(table);

            BigDecimal total = filtrados.stream()
                    .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pagado = filtrados.stream()
                    .map(p -> p.getAdelanto() != null ? p.getAdelanto() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal saldo = filtrados.stream()
                    .map(ReportePedidosPdfService::saldoPedido)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable totals = new PdfPTable(new float[]{1f, 0.35f, 0.35f, 0.35f});
            totals.setWidthPercentage(100);
            totals.setSpacingBefore(10);
            addTotalCell(totals, "Totales", totalFont, Element.ALIGN_RIGHT);
            addTotalCell(totals, "Total S/ " + fmtMoney(total), totalFont, Element.ALIGN_RIGHT);
            addTotalCell(totals, "Pagado S/ " + fmtMoney(pagado), totalFont, Element.ALIGN_RIGHT);
            addTotalCell(totals, "Saldo S/ " + fmtMoney(saldo), totalFont, Element.ALIGN_RIGHT);
            document.add(totals);
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
        c.setPadding(6);
        c.setBackgroundColor(new Color(15, 23, 42));
        c.setBorderColor(new Color(203, 213, 225));
        table.addCell(c);
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        addCell(table, text, font, Element.ALIGN_LEFT);
    }

    private static void addCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setPadding(5);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(align);
        c.setBorderColor(new Color(226, 232, 240));
        table.addCell(c);
    }

    private static void addTotalCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(7);
        c.setHorizontalAlignment(align);
        c.setBackgroundColor(new Color(248, 250, 252));
        c.setBorderColor(new Color(203, 213, 225));
        table.addCell(c);
    }

    private static String filtrosAplicados(ReportePedidosFiltro f) {
        StringBuilder sb = new StringBuilder();
        appendFiltro(sb, "Cliente", f.cliente());
        appendFiltro(sb, "Vendedora", f.vendedora());
        appendFiltro(sb, "Estado", f.estado());
        appendFiltro(sb, "Maquina", f.maquina());
        appendFiltro(sb, "Pago", f.saldo());
        appendFiltro(sb, "Ingreso desde", f.fiDesde() != null ? f.fiDesde().format(DF) : null);
        appendFiltro(sb, "Ingreso hasta", f.fiHasta() != null ? f.fiHasta().format(DF) : null);
        appendFiltro(sb, "Entrega desde", f.feDesde() != null ? f.feDesde().format(DF) : null);
        appendFiltro(sb, "Entrega hasta", f.feHasta() != null ? f.feHasta().format(DF) : null);
        return sb.length() == 0 ? "Sin filtros" : sb.toString();
    }

    private static void appendFiltro(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(label).append(": ").append(value);
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
