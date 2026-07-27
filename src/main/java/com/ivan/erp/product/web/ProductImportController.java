package com.ivan.erp.product.web;

import com.ivan.erp.importing.DataImportService;
import com.ivan.erp.importing.ImportErrorExportService;
import com.ivan.erp.importing.ImportFileException;
import com.ivan.erp.importing.ImportPreview;
import com.ivan.erp.importing.ImportResult;
import com.ivan.erp.importing.ImportTemplateService;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/products/import")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProductImportController {

    private static final String SESSION_FILE = "productImportPendingFile";
    private static final String SESSION_FILENAME = "productImportPendingFilename";
    private static final String SESSION_RESULT = "productImportLastResult";

    private final DataImportService dataImportService;
    private final ImportTemplateService templateService;
    private final ImportErrorExportService errorExportService;

    public ProductImportController(
            DataImportService dataImportService,
            ImportTemplateService templateService,
            ImportErrorExportService errorExportService
    ) {
        this.dataImportService = dataImportService;
        this.templateService = templateService;
        this.errorExportService = errorExportService;
    }

    @GetMapping
    public String form() {
        return "products/import";
    }

    @PostMapping({"", "/preview"})
    public String preview(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
        clearPending(session);
        try {
            ImportPreview preview = dataImportService.previewProducts(file);
            session.setAttribute(SESSION_FILE, file.getBytes());
            session.setAttribute(SESSION_FILENAME, preview.filename());
            model.addAttribute("importPreview", preview);
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "No se ha podido preparar el archivo para la importación.");
        }
        return "products/import";
    }

    @PostMapping("/confirm")
    public String confirm(Model model, HttpSession session) {
        byte[] content = (byte[]) session.getAttribute(SESSION_FILE);
        String filename = (String) session.getAttribute(SESSION_FILENAME);
        if (content == null || filename == null) {
            model.addAttribute("errorMessage", "La previsualización ha caducado. Selecciona el archivo de nuevo.");
            return "products/import";
        }

        try {
            ImportResult result = dataImportService.importProducts(filename, content);
            model.addAttribute("importResult", result);
            model.addAttribute("uploadedFileName", filename);
            session.setAttribute(SESSION_RESULT, result);
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } finally {
            clearPending(session);
        }
        return "products/import";
    }

    @PostMapping("/cancel")
    public String cancel(HttpSession session, RedirectAttributes redirectAttributes) {
        clearPending(session);
        redirectAttributes.addFlashAttribute("successMessage", "Importación cancelada. No se ha creado ningún producto.");
        return "redirect:/products/import";
    }

    @GetMapping("/errors.csv")
    public ResponseEntity<byte[]> errors(HttpSession session) {
        ImportResult result = (ImportResult) session.getAttribute(SESSION_RESULT);
        if (result == null || !result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("errores-importacion-productos.csv", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(errorExportService.toCsv(result));
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

    private void clearPending(HttpSession session) {
        session.removeAttribute(SESSION_FILE);
        session.removeAttribute(SESSION_FILENAME);
    }
}
