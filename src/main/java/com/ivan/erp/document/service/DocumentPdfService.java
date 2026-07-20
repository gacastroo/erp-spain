package com.ivan.erp.document.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.company.Company;
import com.ivan.erp.company.service.CompanySettingsService;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceLine;
import com.ivan.erp.quote.Quote;
import com.ivan.erp.quote.QuoteLine;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DocumentPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_LOCALE = Locale.of("es", "ES");

    private final CompanySettingsService companySettingsService;

    public DocumentPdfService(CompanySettingsService companySettingsService) {
        this.companySettingsService = companySettingsService;
    }

    public byte[] invoicePdf(Invoice invoice) {
        Company company = companySettingsService.getActiveCompany();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            addHeader(document, company, "Factura", invoice.getInvoiceNumber(), invoice.getStatus().getLabel());
            addCompanyAndClient(document, company, invoice.getClient());
            addInvoiceDates(document, invoice);
            addInvoiceLines(document, invoice);
            addTotals(document, invoice.getSubtotal(), invoice.getVatTotal(), invoice.getTotal());
            addPaymentInfo(document, company);
            addNotes(document, invoice.getNotes());
            addLegalText(document, company);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", ex);
        }
    }

    public byte[] quotePdf(Quote quote) {
        Company company = companySettingsService.getActiveCompany();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            addHeader(document, company, "Presupuesto", quote.getQuoteNumber(), quote.getStatus().getLabel());
            addCompanyAndClient(document, company, quote.getClient());
            addQuoteDates(document, quote);
            addQuoteLines(document, quote);
            addTotals(document, quote.getSubtotal(), quote.getVatTotal(), quote.getTotal());
            addNotes(document, quote.getNotes());
            addLegalText(document, company);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF del presupuesto", ex);
        }
    }

    private void addHeader(Document document, Company company, String documentType, String number, String status) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        Cell logoCell = new Cell()
                .add(new Paragraph(company.getLogoText() != null ? company.getLogoText() : "ERP")
                        .setBold()
                        .setFontSize(20)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(14);
        header.addCell(logoCell);

        Cell titleCell = new Cell()
                .add(new Paragraph(documentType).setBold().setFontSize(22).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(number).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph("Estado: " + status).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(null)
                .setPadding(0);
        header.addCell(titleCell);
        header.setMarginBottom(18);
        document.add(header);
    }

    private void addCompanyAndClient(Document document, Company company, Client client) {
        Table parties = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        parties.addCell(infoBox("Empresa emisora",
                company.getLegalName(),
                "NIF/CIF: " + safe(company.getTaxId()),
                addressLine(company.getAddressLine(), company.getPostalCode(), company.getCity(), company.getProvince(), company.getCountry()),
                contactLine(company.getEmail(), company.getPhone())
        ));
        parties.addCell(infoBox("Cliente",
                client.getLegalName(),
                "NIF/CIF: " + safe(client.getTaxId()),
                addressLine(client.getAddressLine(), client.getPostalCode(), client.getCity(), client.getProvince(), client.getCountry()),
                contactLine(client.getEmail(), client.getPhone())
        ));
        parties.setMarginBottom(16);
        document.add(parties);
    }

    private Cell infoBox(String title, String... lines) {
        Cell cell = new Cell().setPadding(10);
        cell.add(new Paragraph(title).setBold().setFontSize(11).setMarginBottom(6));
        for (String line : lines) {
            if (line != null && !line.isBlank() && !line.equals("-")) {
                cell.add(new Paragraph(line).setFontSize(9).setMarginBottom(2));
            }
        }
        return cell;
    }

    private void addInvoiceDates(Document document, Invoice invoice) {
        Table table = metaTable();
        addMetaRow(table, "Fecha de emisión", formatDate(invoice.getIssueDate()));
        addMetaRow(table, "Vencimiento", formatDate(invoice.getDueDate()));
        if (invoice.getQuote() != null) {
            addMetaRow(table, "Presupuesto origen", invoice.getQuote().getQuoteNumber());
        }
        table.setMarginBottom(16);
        document.add(table);
    }

    private void addQuoteDates(Document document, Quote quote) {
        Table table = metaTable();
        addMetaRow(table, "Fecha de emisión", formatDate(quote.getIssueDate()));
        addMetaRow(table, "Válido hasta", formatDate(quote.getValidUntil()));
        table.setMarginBottom(16);
        document.add(table);
    }

    private Table metaTable() {
        return new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
    }

    private void addMetaRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(9)).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(safe(value)).setFontSize(9)).setPadding(6));
    }

    private void addInvoiceLines(Document document, Invoice invoice) {
        Table table = lineTable();
        addLineHeader(table);
        for (InvoiceLine line : invoice.getLines()) {
            addLine(table, line.getDescription(), line.getQuantity(), line.getUnitPrice(), line.getVatRate(), line.getLineSubtotal(), line.getLineTotal());
        }
        table.setMarginBottom(14);
        document.add(table);
    }

    private void addQuoteLines(Document document, Quote quote) {
        Table table = lineTable();
        addLineHeader(table);
        for (QuoteLine line : quote.getLines()) {
            addLine(table, line.getDescription(), line.getQuantity(), line.getUnitPrice(), line.getVatRate(), line.getLineSubtotal(), line.getLineTotal());
        }
        table.setMarginBottom(14);
        document.add(table);
    }

    private Table lineTable() {
        return new Table(UnitValue.createPercentArray(new float[]{4, 1, 1.4f, 1, 1.4f, 1.4f})).useAllAvailableWidth();
    }

    private void addLineHeader(Table table) {
        addHeaderCell(table, "Descripción");
        addHeaderCell(table, "Cant.");
        addHeaderCell(table, "Precio");
        addHeaderCell(table, "IVA");
        addHeaderCell(table, "Base");
        addHeaderCell(table, "Total");
    }

    private void addHeaderCell(Table table, String value) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(value).setBold().setFontSize(8))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(6));
    }

    private void addLine(Table table, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal vatRate, BigDecimal subtotal, BigDecimal total) {
        table.addCell(new Cell().add(new Paragraph(safe(description)).setFontSize(8)).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(number(quantity)).setFontSize(8)).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(money(unitPrice)).setFontSize(8)).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(number(vatRate) + " %").setFontSize(8)).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(money(subtotal)).setFontSize(8)).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(money(total)).setFontSize(8)).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addTotals(Document document, BigDecimal subtotal, BigDecimal vatTotal, BigDecimal total) {
        Table totals = new Table(UnitValue.createPercentArray(new float[]{2, 1})).setWidth(UnitValue.createPercentValue(45)).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
        addTotalRow(totals, "Base imponible", subtotal, false);
        addTotalRow(totals, "IVA", vatTotal, false);
        addTotalRow(totals, "Total", total, true);
        totals.setMarginBottom(16);
        document.add(totals);
    }

    private void addTotalRow(Table table, String label, BigDecimal value, boolean strong) {
        Paragraph labelParagraph = new Paragraph(label).setFontSize(strong ? 11 : 9);
        Paragraph valueParagraph = new Paragraph(money(value)).setFontSize(strong ? 11 : 9).setTextAlignment(TextAlignment.RIGHT);
        if (strong) {
            labelParagraph.setBold();
            valueParagraph.setBold();
        }
        table.addCell(new Cell().add(labelParagraph).setPadding(6));
        table.addCell(new Cell().add(valueParagraph).setPadding(6));
    }

    private void addPaymentInfo(Document document, Company company) {
        if ((company.getBankName() == null || company.getBankName().isBlank())
                && (company.getBankIban() == null || company.getBankIban().isBlank())) {
            return;
        }
        document.add(new Paragraph("Datos de pago").setBold().setFontSize(11).setMarginBottom(4));
        if (company.getBankName() != null) {
            document.add(new Paragraph("Banco: " + company.getBankName()).setFontSize(9).setMarginBottom(2));
        }
        if (company.getBankIban() != null) {
            document.add(new Paragraph("IBAN: " + company.getBankIban()).setFontSize(9).setMarginBottom(10));
        }
    }

    private void addNotes(Document document, String notes) {
        if (notes == null || notes.isBlank()) {
            return;
        }
        document.add(new Paragraph("Notas").setBold().setFontSize(11).setMarginTop(8).setMarginBottom(4));
        document.add(new Paragraph(notes).setFontSize(9).setMarginBottom(10));
    }

    private void addLegalText(Document document, Company company) {
        if (company.getInvoiceLegalText() == null || company.getInvoiceLegalText().isBlank()) {
            return;
        }
        document.add(new Paragraph(company.getInvoiceLegalText()).setFontSize(8).setMarginTop(12));
    }

    private String addressLine(String address, String postalCode, String city, String province, String country) {
        StringBuilder builder = new StringBuilder();
        append(builder, address);
        append(builder, joinPostalCity(postalCode, city));
        append(builder, province);
        append(builder, country);
        return builder.isEmpty() ? "-" : builder.toString();
    }

    private String joinPostalCity(String postalCode, String city) {
        if ((postalCode == null || postalCode.isBlank()) && (city == null || city.isBlank())) {
            return null;
        }
        return safe(postalCode) + " " + safe(city);
    }

    private String contactLine(String email, String phone) {
        StringBuilder builder = new StringBuilder();
        append(builder, email);
        append(builder, phone);
        return builder.isEmpty() ? null : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" · ");
        }
        builder.append(value);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(ES_LOCALE).format(value != null ? value : BigDecimal.ZERO);
    }

    private String number(BigDecimal value) {
        return NumberFormat.getNumberInstance(ES_LOCALE).format(value != null ? value : BigDecimal.ZERO);
    }
}
