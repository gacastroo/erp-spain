package com.ivan.erp.tax.service;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.service.CompanySettingsService;
import com.ivan.erp.expense.Expense;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.tax.TaxSummary;
import com.ivan.erp.tax.TaxVatRateRow;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class TaxExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_LOCALE = Locale.of("es", "ES");

    private final CompanySettingsService companySettingsService;

    public TaxExportService(CompanySettingsService companySettingsService) {
        this.companySettingsService = companySettingsService;
    }

    public byte[] toExcel(TaxSummary summary) {
        Company company = companySettingsService.getActiveCompany();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            moneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00 €"));

            writeSummary(workbook.createSheet("Resumen IVA"), summary, company, moneyStyle);
            writeVatRates(workbook.createSheet("IVA repercutido"), summary.outputVatByRate(), moneyStyle);
            writeVatRates(workbook.createSheet("IVA soportado"), summary.inputVatByRate(), moneyStyle);
            writeInvoices(workbook.createSheet("Facturas emitidas"), summary, moneyStyle);
            writeExpenses(workbook.createSheet("Gastos"), summary, moneyStyle);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int column = 0; column < 8; column++) {
                    sheet.autoSizeColumn(column);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el Excel de IVA", ex);
        }
    }

    public byte[] toPdf(TaxSummary summary) {
        Company company = companySettingsService.getActiveCompany();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            addCompanyPdfHeader(document, company);

            document.add(new Paragraph("Resumen trimestral de IVA")
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(8));
            document.add(new Paragraph("Periodo: " + summary.year() + " T" + summary.quarter()
                    + " · " + formatDate(summary.startDate()) + " - " + formatDate(summary.endDate()))
                    .setFontSize(10)
                    .setMarginBottom(18));

            Table summaryTable = createPdfTable(2);
            addPdfRow(summaryTable, "Base facturas emitidas", formatMoney(summary.issuedBase()));
            addPdfRow(summaryTable, "IVA repercutido", formatMoney(summary.issuedVat()));
            addPdfRow(summaryTable, "Total facturado", formatMoney(summary.issuedTotal()));
            addPdfRow(summaryTable, "Base gastos deducibles", formatMoney(summary.deductibleBase()));
            addPdfRow(summaryTable, "IVA soportado", formatMoney(summary.deductibleVat()));
            addPdfRow(summaryTable, "Total gastos", formatMoney(summary.deductibleTotal()));
            addPdfRow(summaryTable, summary.isVatToPay() ? "IVA a ingresar" : "IVA a compensar", formatMoney(summary.vatResult().abs()));
            summaryTable.setMarginBottom(18);
            document.add(summaryTable);

            addSectionTitle(document, "IVA repercutido por tipo");
            document.add(vatRateTable(summary.outputVatByRate()));

            addSectionTitle(document, "IVA soportado por tipo");
            document.add(vatRateTable(summary.inputVatByRate()));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF de IVA", ex);
        }
    }

    private void writeSummary(Sheet sheet, TaxSummary summary, Company company, CellStyle moneyStyle) {
        int rowIndex = 0;
        rowIndex = writeCompanyExcelHeader(sheet, company, rowIndex);
        rowIndex++;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("Resumen trimestral de IVA");
        Row period = sheet.createRow(rowIndex++);
        period.createCell(0).setCellValue("Periodo");
        period.createCell(1).setCellValue(summary.year() + " T" + summary.quarter());
        Row dates = sheet.createRow(rowIndex++);
        dates.createCell(0).setCellValue("Fechas");
        dates.createCell(1).setCellValue(formatDate(summary.startDate()) + " - " + formatDate(summary.endDate()));
        rowIndex++;
        writeMoney(sheet, rowIndex++, "Base facturas emitidas", summary.issuedBase(), moneyStyle);
        writeMoney(sheet, rowIndex++, "IVA repercutido", summary.issuedVat(), moneyStyle);
        writeMoney(sheet, rowIndex++, "Total facturado", summary.issuedTotal(), moneyStyle);
        writeMoney(sheet, rowIndex++, "Base gastos deducibles", summary.deductibleBase(), moneyStyle);
        writeMoney(sheet, rowIndex++, "IVA soportado", summary.deductibleVat(), moneyStyle);
        writeMoney(sheet, rowIndex++, "Total gastos", summary.deductibleTotal(), moneyStyle);
        writeMoney(sheet, rowIndex++, summary.isVatToPay() ? "IVA a ingresar" : "IVA a compensar", summary.vatResult().abs(), moneyStyle);
    }

    private void writeVatRates(Sheet sheet, Iterable<TaxVatRateRow> rows, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Tipo IVA");
        header.createCell(1).setCellValue("Base");
        header.createCell(2).setCellValue("IVA");
        header.createCell(3).setCellValue("Total");
        for (TaxVatRateRow row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            excelRow.createCell(0).setCellValue(row.vatRate().doubleValue() + "%");
            writeMoneyCell(excelRow, 1, row.taxableBase(), moneyStyle);
            writeMoneyCell(excelRow, 2, row.vatAmount(), moneyStyle);
            writeMoneyCell(excelRow, 3, row.total(), moneyStyle);
        }
    }

    private void writeInvoices(Sheet sheet, TaxSummary summary, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Fecha");
        header.createCell(1).setCellValue("Factura");
        header.createCell(2).setCellValue("Cliente");
        header.createCell(3).setCellValue("Estado");
        header.createCell(4).setCellValue("Base");
        header.createCell(5).setCellValue("IVA");
        header.createCell(6).setCellValue("Total");
        for (Invoice invoice : summary.invoices()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(formatDate(invoice.getIssueDate()));
            row.createCell(1).setCellValue(invoice.getInvoiceNumber());
            row.createCell(2).setCellValue(invoice.getClient().getLegalName());
            row.createCell(3).setCellValue(invoice.getStatus().getLabel());
            writeMoneyCell(row, 4, invoice.getSubtotal(), moneyStyle);
            writeMoneyCell(row, 5, invoice.getVatTotal(), moneyStyle);
            writeMoneyCell(row, 6, invoice.getTotal(), moneyStyle);
        }
    }

    private void writeExpenses(Sheet sheet, TaxSummary summary, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Fecha");
        header.createCell(1).setCellValue("Proveedor");
        header.createCell(2).setCellValue("Nº factura");
        header.createCell(3).setCellValue("Categoría");
        header.createCell(4).setCellValue("Base");
        header.createCell(5).setCellValue("IVA");
        header.createCell(6).setCellValue("Total");
        for (Expense expense : summary.expenses()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(formatDate(expense.getExpenseDate()));
            row.createCell(1).setCellValue(expense.getSupplierName());
            row.createCell(2).setCellValue(expense.getInvoiceNumber() != null ? expense.getInvoiceNumber() : "-");
            row.createCell(3).setCellValue(expense.getCategory().getLabel());
            writeMoneyCell(row, 4, expense.getBaseAmount(), moneyStyle);
            writeMoneyCell(row, 5, expense.getVatAmount(), moneyStyle);
            writeMoneyCell(row, 6, expense.getTotal(), moneyStyle);
        }
    }

    private Table vatRateTable(Iterable<TaxVatRateRow> rows) {
        Table table = createPdfTable(4);
        addPdfHeader(table, "Tipo", "Base", "IVA", "Total");
        for (TaxVatRateRow row : rows) {
            addPdfRow(table, row.vatRate().toPlainString() + "%", formatMoney(row.taxableBase()), formatMoney(row.vatAmount()), formatMoney(row.total()));
        }
        table.setMarginBottom(18);
        return table;
    }

    private Table createPdfTable(int columns) {
        return new Table(UnitValue.createPercentArray(columns)).useAllAvailableWidth();
    }

    private void addPdfHeader(Table table, String... values) {
        for (String value : values) {
            table.addHeaderCell(new Cell().add(new Paragraph(value)).setBold().setTextAlignment(TextAlignment.LEFT));
        }
    }

    private void addPdfRow(Table table, String... values) {
        for (String value : values) {
            table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")));
        }
    }

    private void addSectionTitle(Document document, String title) {
        document.add(new Paragraph(title).setBold().setFontSize(13).setMarginTop(8).setMarginBottom(8));
    }

    private void writeMoney(Sheet sheet, int rowIndex, String label, BigDecimal value, CellStyle moneyStyle) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        writeMoneyCell(row, 1, value, moneyStyle);
    }

    private void writeMoneyCell(Row row, int columnIndex, BigDecimal value, CellStyle moneyStyle) {
        row.createCell(columnIndex).setCellValue(value != null ? value.doubleValue() : 0D);
        row.getCell(columnIndex).setCellStyle(moneyStyle);
    }


    private int writeCompanyExcelHeader(Sheet sheet, Company company, int rowIndex) {
        Row companyRow = sheet.createRow(rowIndex++);
        companyRow.createCell(0).setCellValue("Empresa");
        companyRow.createCell(1).setCellValue(companyName(company));

        Row taxRow = sheet.createRow(rowIndex++);
        taxRow.createCell(0).setCellValue("NIF/CIF");
        taxRow.createCell(1).setCellValue(safe(company.getTaxId()));

        Row addressRow = sheet.createRow(rowIndex++);
        addressRow.createCell(0).setCellValue("Dirección");
        addressRow.createCell(1).setCellValue(companyAddress(company));

        Row contactRow = sheet.createRow(rowIndex++);
        contactRow.createCell(0).setCellValue("Contacto");
        contactRow.createCell(1).setCellValue(contactLine(company.getEmail(), company.getPhone()));

        return rowIndex;
    }

    private void addCompanyPdfHeader(Document document, Company company) {
        Table companyTable = createPdfTable(2);
        addPdfRow(companyTable, "Empresa", companyName(company));
        addPdfRow(companyTable, "NIF/CIF", safe(company.getTaxId()));
        addPdfRow(companyTable, "Dirección", companyAddress(company));
        addPdfRow(companyTable, "Contacto", contactLine(company.getEmail(), company.getPhone()));
        companyTable.setMarginBottom(16);
        document.add(companyTable);
    }

    private String companyName(Company company) {
        if (hasText(company.getCommercialName())) {
            return company.getCommercialName() + " · " + safe(company.getLegalName());
        }
        return safe(company.getLegalName());
    }

    private String companyAddress(Company company) {
        StringBuilder builder = new StringBuilder();
        append(builder, company.getAddressLine());
        append(builder, joinPostalCity(company.getPostalCode(), company.getCity()));
        append(builder, company.getProvince());
        append(builder, company.getCountry());
        return builder.isEmpty() ? "-" : builder.toString();
    }

    private String joinPostalCity(String postalCode, String city) {
        if (!hasText(postalCode) && !hasText(city)) {
            return null;
        }
        if (!hasText(postalCode)) {
            return city.trim();
        }
        if (!hasText(city)) {
            return postalCode.trim();
        }
        return postalCode.trim() + " " + city.trim();
    }

    private String contactLine(String email, String phone) {
        StringBuilder builder = new StringBuilder();
        append(builder, email);
        append(builder, phone);
        return builder.isEmpty() ? "-" : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (!hasText(value)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" · ");
        }
        builder.append(value.trim());
    }

    private String safe(String value) {
        return hasText(value) ? value.trim() : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String formatMoney(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(ES_LOCALE).format(value != null ? value : BigDecimal.ZERO);
    }
}
