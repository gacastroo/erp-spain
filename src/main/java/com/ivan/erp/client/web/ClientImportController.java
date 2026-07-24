package com.ivan.erp.client.web;

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
@RequestMapping("/clients/import")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ClientImportController {

    private final DataImportService dataImportService;
    private final ImportTemplateService templateService;

    public ClientImportController(DataImportService dataImportService, ImportTemplateService templateService) {
        this.dataImportService = dataImportService;
        this.templateService = templateService;
    }

    @GetMapping
    public String form() {
        return "clients/import";
    }

    @PostMapping
    public String importFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ImportResult result = dataImportService.importClients(file);
            model.addAttribute("importResult", result);
            model.addAttribute("uploadedFileName", file.getOriginalFilename());
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return "clients/import";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(@RequestParam(defaultValue = "xlsx") String format) {
        boolean csv = "csv".equalsIgnoreCase(format);
        String normalizedFormat = csv ? "csv" : "xlsx";
        byte[] content = templateService.clientTemplate(normalizedFormat);
        String filename = "plantilla-clientes." + normalizedFormat;

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
