package com.ivan.erp.tax.web;

import com.ivan.erp.tax.TaxSummary;
import com.ivan.erp.tax.service.TaxExportService;
import com.ivan.erp.tax.service.TaxService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/taxes")
public class TaxController {

    private final TaxService taxService;
    private final TaxExportService taxExportService;

    public TaxController(TaxService taxService, TaxExportService taxExportService) {
        this.taxService = taxService;
        this.taxExportService = taxExportService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            Model model
    ) {
        TaxSummary summary = loadSummary(year, quarter);
        model.addAttribute("summary", summary);
        model.addAttribute("years", availableYears());
        model.addAttribute("quarters", List.of(1, 2, 3, 4));
        return "taxes/index";
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter
    ) {
        TaxSummary summary = loadSummary(year, quarter);
        byte[] content = taxExportService.toExcel(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=iva-" + summary.year() + "-t" + summary.quarter() + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter
    ) {
        TaxSummary summary = loadSummary(year, quarter);
        byte[] content = taxExportService.toPdf(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=iva-" + summary.year() + "-t" + summary.quarter() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    private TaxSummary loadSummary(Integer year, Integer quarter) {
        LocalDate today = LocalDate.now();
        int selectedYear = year != null ? year : today.getYear();
        int selectedQuarter = quarter != null ? quarter : ((today.getMonthValue() - 1) / 3) + 1;
        return taxService.buildQuarterSummary(selectedYear, selectedQuarter);
    }

    private List<Integer> availableYears() {
        int currentYear = LocalDate.now().getYear();
        return IntStream.rangeClosed(currentYear - 5, currentYear + 1)
                .boxed()
                .sorted((a, b) -> Integer.compare(b, a))
                .toList();
    }
}
