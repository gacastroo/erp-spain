package com.ivan.erp.product.web;

import com.ivan.erp.importing.DataImportService;
import com.ivan.erp.importing.ImportFileException;
import com.ivan.erp.importing.ImportResult;
import com.ivan.erp.importing.ImportTemplateService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/products/import")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProductImportController {

    private final DataImportService dataImportService;
    private final ImportTemplateService templateService;

    public ProductImportController(DataImportService dataImportService, ImportTemplateService templateService) {
        this.dataImportService = dataImportService;
        this.templateService = templateService;
    }

    @GetMapping
    public String form() {
        return "products/import";
    }

    @PostMapping
    public String importFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ImportResult result = dataImportService.importProducts(file);
            model.addAttribute("importResult", result);
            model.addAttribute("uploadedFileName", file.getOriginalFilename());
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return "products/import";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(@RequestParam(defaultValue = "xlsx") String format) {
        boolean csv = "csv".equalsIgnoreCase(format);
        String normalizedFormat = csv ? "csv" : "xlsx";
        byte[] content = templateService.productTemplate(normalizedFormat);
        String filename = "plantilla-productos." + normalizedFormat;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(csv
                        ? new MediaType("text", "csv", StandardCharsets.UTF_8)
                        : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}
