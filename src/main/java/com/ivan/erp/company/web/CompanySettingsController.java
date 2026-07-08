package com.ivan.erp.company.web;

import com.ivan.erp.company.service.CompanySettingsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/company")
@PreAuthorize("hasRole('ADMIN')")
public class CompanySettingsController {

    private final CompanySettingsService companySettingsService;

    public CompanySettingsController(CompanySettingsService companySettingsService) {
        this.companySettingsService = companySettingsService;
    }

    @GetMapping
    public String edit(Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("companySettingsForm")) {
                model.addAttribute("companySettingsForm", companySettingsService.getSettingsForm());
            }
            return "settings/company";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping
    public String update(
            @Valid @ModelAttribute("companySettingsForm") CompanySettingsForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "settings/company";
        }

        companySettingsService.update(form);
        redirectAttributes.addFlashAttribute("successMessage", "Configuración de empresa actualizada correctamente");
        return "redirect:/settings/company";
    }
}
