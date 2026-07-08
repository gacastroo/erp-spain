package com.ivan.erp.client.web;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientType;
import com.ivan.erp.client.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String index(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<Client> clients = clientService.search(query, page, size);

        model.addAttribute("clients", clients);
        model.addAttribute("q", query);
        model.addAttribute("size", size);

        return "clients/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("clientForm", new ClientForm());
        model.addAttribute("clientTypes", ClientType.values());
        model.addAttribute("pageTitle", "Nuevo cliente");
        model.addAttribute("formAction", "/clients");
        model.addAttribute("isEdit", false);
        return "clients/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(
            @Valid @ModelAttribute("clientForm") ClientForm clientForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Nuevo cliente", "/clients", false);
            return "clients/form";
        }

        try {
            Client client = clientService.create(clientForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente creado correctamente");
            return "redirect:/clients/" + client.getId() + "/edit";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("taxId", "duplicate", ex.getMessage());
            prepareForm(model, "Nuevo cliente", "/clients", false);
            return "clients/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Client client = clientService.getById(id);
            model.addAttribute("clientForm", ClientForm.from(client));
            model.addAttribute("client", client);
            prepareForm(model, "Editar cliente", "/clients/" + id, true);
            return "clients/form";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
            return "redirect:/clients";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("clientForm") ClientForm clientForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("client", clientService.getById(id));
            } catch (EntityNotFoundException ignored) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
                return "redirect:/clients";
            }
            prepareForm(model, "Editar cliente", "/clients/" + id, true);
            return "clients/form";
        }

        try {
            clientService.update(id, clientForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente actualizado correctamente");
            return "redirect:/clients/" + id + "/edit";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("taxId", "duplicate", ex.getMessage());
            model.addAttribute("client", clientService.getById(id));
            prepareForm(model, "Editar cliente", "/clients/" + id, true);
            return "clients/form";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
            return "redirect:/clients";
        }
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String toggle(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes
    ) {
        try {
            clientService.setEnabled(id, enabled);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    enabled ? "Cliente activado correctamente" : "Cliente desactivado correctamente"
            );
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
        }

        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            clientService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "No se puede eliminar este cliente porque tiene datos relacionados. Puedes desactivarlo."
            );
        }

        return "redirect:/clients";
    }

    private void prepareForm(Model model, String pageTitle, String formAction, boolean isEdit) {
        model.addAttribute("clientTypes", ClientType.values());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
        model.addAttribute("isEdit", isEdit);
    }
}
