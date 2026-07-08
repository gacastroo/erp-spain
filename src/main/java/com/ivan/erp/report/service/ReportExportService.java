package com.ivan.erp.report.service;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.payment.Payment;
import com.ivan.erp.report.ReportData;
import com.ivan.erp.report.SalesClientRow;
import com.ivan.erp.report.SalesProductRow;
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
public class ReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_LOCALE = Locale.of("es", "ES");

    public byte[] toExcel(ReportData reportData) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            moneyStyle.setDataFormat(dataFormat.getFormat("#,##0.00 €"));

            Sheet summary = workbook.createSheet("Resumen");
            writeSummary(summary, reportData, moneyStyle);

            Sheet invoices = workbook.createSheet("Facturas");
            writeInvoices(invoices, reportData, moneyStyle);

            Sheet payments = workbook.createSheet("Cobros");
            writePayments(payments, reportData, moneyStyle);

            Sheet expenses = workbook.createSheet("Gastos");
            writeExpenses(expenses, reportData, moneyStyle);

            Sheet clients = workbook.createSheet("Clientes");
            writeClients(clients, reportData, moneyStyle);

            Sheet products = workbook.createSheet("Productos");
            writeProducts(products, reportData, moneyStyle);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int column = 0; column < 8; column++) {
                    sheet.autoSizeColumn(column);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el Excel", ex);
        }
    }

    public byte[] toPdf(ReportData reportData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            Paragraph title = new Paragraph("Reporte de ventas, cobros y gastos")
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(8);
            document.add(title);

            Paragraph period = new Paragraph("Periodo: " + formatDate(reportData.startDate()) + " - " + formatDate(reportData.endDate()))
                    .setFontSize(10)
                    .setMarginBottom(18);
            document.add(period);

            Table summaryTable = createPdfTable(2);
            addPdfRow(summaryTable, "Facturado", formatMoney(reportData.invoicedTotal()));
            addPdfRow(summaryTable, "Cobrado", formatMoney(reportData.collectedTotal()));
            addPdfRow(summaryTable, "Gastos", formatMoney(reportData.expensesTotal()));
            addPdfRow(summaryTable, "Resultado de caja", formatMoney(reportData.cashResult()));
            addPdfRow(summaryTable, "Pendiente estimado", formatMoney(reportData.pendingEstimated()));
            addPdfRow(summaryTable, "Facturas", String.valueOf(reportData.invoiceCount()));
            addPdfRow(summaryTable, "Cobros", String.valueOf(reportData.paymentCount()));
            addPdfRow(summaryTable, "Gastos registrados", String.valueOf(reportData.expenseCount()));
            summaryTable.setMarginBottom(20);
            document.add(summaryTable);

            addSectionTitle(document, "Ventas por cliente");
            Table clientTable = createPdfTable(3);
            addPdfHeader(clientTable, "Cliente", "Facturas", "Total");
            for (SalesClientRow row : reportData.salesByClient()) {
                addPdfRow(clientTable, row.clientName(), String.valueOf(row.invoiceCount()), formatMoney(row.total()));
            }
            clientTable.setMarginBottom(18);
            document.add(clientTable);

            addSectionTitle(document, "Productos y servicios vendidos");
            Table productTable = createPdfTable(3);
            addPdfHeader(productTable, "Concepto", "Cantidad", "Total");
            for (SalesProductRow row : reportData.salesByProduct()) {
                addPdfRow(productTable, row.description(), row.quantity().toPlainString(), formatMoney(row.total()));
            }
            productTable.setMarginBottom(18);
            document.add(productTable);

            addSectionTitle(document, "Gastos del periodo");
            Table expenseTable = createPdfTable(4);
            addPdfHeader(expenseTable, "Fecha", "Proveedor", "Categoría", "Total");
            for (Expense expense : reportData.expenses()) {
                addPdfRow(
                        expenseTable,
                        formatDate(expense.getExpenseDate()),
                        expense.getSupplierName(),
                        expense.getCategory().getLabel(),
                        formatMoney(expense.getTotal())
                );
            }
            document.add(expenseTable);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        }
    }

    private void writeSummary(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("Reporte de ventas, cobros y gastos");
        Row period = sheet.createRow(rowIndex++);
        period.createCell(0).setCellValue("Periodo");
        period.createCell(1).setCellValue(formatDate(reportData.startDate()) + " - " + formatDate(reportData.endDate()));
        rowIndex++;
        writeMetric(sheet, rowIndex++, "Facturado", reportData.invoicedTotal(), moneyStyle);
        writeMetric(sheet, rowIndex++, "Cobrado", reportData.collectedTotal(), moneyStyle);
        writeMetric(sheet, rowIndex++, "Gastos", reportData.expensesTotal(), moneyStyle);
        writeMetric(sheet, rowIndex++, "Resultado de caja", reportData.cashResult(), moneyStyle);
        writeMetric(sheet, rowIndex++, "Pendiente estimado", reportData.pendingEstimated(), moneyStyle);
        Row invoices = sheet.createRow(rowIndex++);
        invoices.createCell(0).setCellValue("Facturas");
        invoices.createCell(1).setCellValue(reportData.invoiceCount());
        Row payments = sheet.createRow(rowIndex++);
        payments.createCell(0).setCellValue("Cobros");
        payments.createCell(1).setCellValue(reportData.paymentCount());
        Row expenses = sheet.createRow(rowIndex);
        expenses.createCell(0).setCellValue("Gastos registrados");
        expenses.createCell(1).setCellValue(reportData.expenseCount());
    }

    private void writeInvoices(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Factura");
        header.createCell(1).setCellValue("Cliente");
        header.createCell(2).setCellValue("Fecha");
        header.createCell(3).setCellValue("Vencimiento");
        header.createCell(4).setCellValue("Estado");
        header.createCell(5).setCellValue("Total");

        for (Invoice invoice : reportData.invoices()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getClient().getLegalName());
            row.createCell(2).setCellValue(formatDate(invoice.getIssueDate()));
            row.createCell(3).setCellValue(invoice.getDueDate() != null ? formatDate(invoice.getDueDate()) : "-");
            row.createCell(4).setCellValue(invoice.getStatus().getLabel());
            row.createCell(5).setCellValue(invoice.getTotal().doubleValue());
            row.getCell(5).setCellStyle(moneyStyle);
        }
    }

    private void writePayments(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Fecha");
        header.createCell(1).setCellValue("Factura");
        header.createCell(2).setCellValue("Cliente");
        header.createCell(3).setCellValue("Método");
        header.createCell(4).setCellValue("Referencia");
        header.createCell(5).setCellValue("Importe");

        for (Payment payment : reportData.payments()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(formatDate(payment.getPaymentDate()));
            row.createCell(1).setCellValue(payment.getInvoice().getInvoiceNumber());
            row.createCell(2).setCellValue(payment.getInvoice().getClient().getLegalName());
            row.createCell(3).setCellValue(payment.getMethod().getLabel());
            row.createCell(4).setCellValue(payment.getReference() != null ? payment.getReference() : "-");
            row.createCell(5).setCellValue(payment.getAmount().doubleValue());
            row.getCell(5).setCellStyle(moneyStyle);
        }
    }

    private void writeExpenses(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Fecha");
        header.createCell(1).setCellValue("Proveedor");
        header.createCell(2).setCellValue("Categoría");
        header.createCell(3).setCellValue("Nº factura");
        header.createCell(4).setCellValue("Base");
        header.createCell(5).setCellValue("IVA");
        header.createCell(6).setCellValue("Total");
        header.createCell(7).setCellValue("Estado");

        for (Expense expense : reportData.expenses()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(formatDate(expense.getExpenseDate()));
            row.createCell(1).setCellValue(expense.getSupplierName());
            row.createCell(2).setCellValue(expense.getCategory().getLabel());
            row.createCell(3).setCellValue(expense.getInvoiceNumber() != null ? expense.getInvoiceNumber() : "-");
            row.createCell(4).setCellValue(expense.getBaseAmount().doubleValue());
            row.getCell(4).setCellStyle(moneyStyle);
            row.createCell(5).setCellValue(expense.getVatAmount().doubleValue());
            row.getCell(5).setCellStyle(moneyStyle);
            row.createCell(6).setCellValue(expense.getTotal().doubleValue());
            row.getCell(6).setCellStyle(moneyStyle);
            row.createCell(7).setCellValue(expense.isPaid() ? "Pagado" : "Pendiente");
        }
    }

    private void writeClients(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Cliente");
        header.createCell(1).setCellValue("Facturas");
        header.createCell(2).setCellValue("Total");

        for (SalesClientRow client : reportData.salesByClient()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(client.clientName());
            row.createCell(1).setCellValue(client.invoiceCount());
            row.createCell(2).setCellValue(client.total().doubleValue());
            row.getCell(2).setCellStyle(moneyStyle);
        }
    }

    private void writeProducts(Sheet sheet, ReportData reportData, CellStyle moneyStyle) {
        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Producto / servicio");
        header.createCell(1).setCellValue("Cantidad");
        header.createCell(2).setCellValue("Total");

        for (SalesProductRow product : reportData.salesByProduct()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(product.description());
            row.createCell(1).setCellValue(product.quantity().doubleValue());
            row.createCell(2).setCellValue(product.total().doubleValue());
            row.getCell(2).setCellStyle(moneyStyle);
        }
    }

    private void writeMetric(Sheet sheet, int rowIndex, String label, BigDecimal value, CellStyle moneyStyle) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value.doubleValue());
        row.getCell(1).setCellStyle(moneyStyle);
    }

    private Table createPdfTable(int columns) {
        Table table = new Table(UnitValue.createPercentArray(columns));
        table.useAllAvailableWidth();
        return table;
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph paragraph = new Paragraph(title)
                .setBold()
                .setFontSize(13)
                .setMarginTop(8)
                .setMarginBottom(8);
        document.add(paragraph);
    }

    private void addPdfHeader(Table table, String... values) {
        for (String value : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(value).setBold().setFontSize(9))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(6);
            table.addCell(cell);
        }
    }

    private void addPdfRow(Table table, String... values) {
        for (String value : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(value != null ? value : "-").setFontSize(9))
                    .setPadding(6);
            table.addCell(cell);
        }
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "-";
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(ES_LOCALE);
        return format.format(value != null ? value : BigDecimal.ZERO);
    }
}
