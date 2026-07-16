package com.ivan.erp.report.web;

import com.ivan.erp.report.ReportData;
import com.ivan.erp.report.service.ReportExportService;
import com.ivan.erp.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    public ReportController(ReportService reportService, ReportExportService reportExportService) {
        this.reportService = reportService;
        this.reportExportService = reportExportService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        ReportData reportData = reportService.buildSalesReport(startDate, endDate);
        model.addAttribute("report", reportData);
        model.addAttribute("startDate", reportData.startDate());
        model.addAttribute("endDate", reportData.endDate());
        return "reports/index";
    }

    @GetMapping("/sales.xlsx")
    public ResponseEntity<byte[]> salesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ReportData reportData = reportService.buildSalesReport(startDate, endDate);
        byte[] content = reportExportService.toExcel(reportData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("reporte-ventas.xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/sales.pdf")
    public ResponseEntity<byte[]> salesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ReportData reportData = reportService.buildSalesReport(startDate, endDate);
        byte[] content = reportExportService.toPdf(reportData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("reporte-ventas.pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    private String contentDisposition(String filename) {
        return ContentDisposition.attachment()
                .filename(filename)
                .build()
                .toString();
    }
}
