package com.ivan.erp.client.web;

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
@RequestMapping("/clients/import")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ClientImportController {

    private static final String SESSION_FILE = "clientImportPendingFile";
    private static final String SESSION_FILENAME = "clientImportPendingFilename";
    private static final String SESSION_RESULT = "clientImportLastResult";

    private final DataImportService dataImportService;
    private final ImportTemplateService templateService;
    private final ImportErrorExportService errorExportService;

    public ClientImportController(
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
        return "clients/import";
    }

    @PostMapping({"", "/preview"})
    public String preview(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
        clearPending(session);
        try {
            ImportPreview preview = dataImportService.previewClients(file);
            session.setAttribute(SESSION_FILE, file.getBytes());
            session.setAttribute(SESSION_FILENAME, preview.filename());
            model.addAttribute("importPreview", preview);
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "No se ha podido preparar el archivo para la importación.");
        }
        return "clients/import";
    }

    @PostMapping("/confirm")
    public String confirm(Model model, HttpSession session) {
        byte[] content = (byte[]) session.getAttribute(SESSION_FILE);
        String filename = (String) session.getAttribute(SESSION_FILENAME);
        if (content == null || filename == null) {
            model.addAttribute("errorMessage", "La previsualización ha caducado. Selecciona el archivo de nuevo.");
            return "clients/import";
        }

        try {
            ImportResult result = dataImportService.importClients(filename, content);
            model.addAttribute("importResult", result);
            model.addAttribute("uploadedFileName", filename);
            session.setAttribute(SESSION_RESULT, result);
        } catch (ImportFileException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } finally {
            clearPending(session);
        }
        return "clients/import";
    }

    @PostMapping("/cancel")
    public String cancel(HttpSession session, RedirectAttributes redirectAttributes) {
        clearPending(session);
        redirectAttributes.addFlashAttribute("successMessage", "Importación cancelada. No se ha creado ningún cliente.");
        return "redirect:/clients/import";
    }

    @GetMapping("/errors.csv")
    public ResponseEntity<byte[]> errors(HttpSession session) {
        ImportResult result = (ImportResult) session.getAttribute(SESSION_RESULT);
        if (result == null || !result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("errores-importacion-clientes.csv", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(errorExportService.toCsv(result));
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

    private void clearPending(HttpSession session) {
        session.removeAttribute(SESSION_FILE);
        session.removeAttribute(SESSION_FILENAME);
    }
}
