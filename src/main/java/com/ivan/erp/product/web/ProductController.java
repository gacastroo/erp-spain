package com.ivan.erp.product.web;

import com.ivan.erp.product.Product;
import com.ivan.erp.product.ProductType;
import com.ivan.erp.product.service.ProductService;
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
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Product> products = productService.search(query, page);

        model.addAttribute("products", products);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);

        return "products/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("pageTitle", "Nuevo producto o servicio");
        model.addAttribute("formAction", "/products");

        return "products/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(
            @Valid @ModelAttribute("productForm") ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateNewSku(productForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("pageTitle", "Nuevo producto o servicio");
            model.addAttribute("formAction", "/products");
            return "products/form";
        }

        productService.create(productForm);
        redirectAttributes.addFlashAttribute("successMessage", "Producto creado correctamente");

        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Product product = productService.getById(id);

            model.addAttribute("productForm", ProductForm.fromProduct(product));
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("productId", id);
            model.addAttribute("pageTitle", "Editar producto o servicio");
            model.addAttribute("formAction", "/products/" + id);

            return "products/form";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado");
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateExistingSku(productForm, id, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("productId", id);
            model.addAttribute("pageTitle", "Editar producto o servicio");
            model.addAttribute("formAction", "/products/" + id);
            return "products/form";
        }

        try {
            productService.update(id, productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Producto actualizado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado");
        }

        return "redirect:/products";
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String toggle(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.setEnabled(id, enabled);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    enabled ? "Producto activado correctamente" : "Producto desactivado correctamente"
            );
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado");
        }

        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "No se puede eliminar este producto porque tiene datos relacionados. Puedes desactivarlo."
            );
        }

        return "redirect:/products";
    }

    private void validateNewSku(ProductForm productForm, BindingResult bindingResult) {
        if (productService.skuExists(productForm.getSku())) {
            bindingResult.rejectValue("sku", "sku.exists", "Ya existe un producto con esta referencia");
        }
    }

    private void validateExistingSku(ProductForm productForm, Long productId, BindingResult bindingResult) {
        if (productService.skuExistsForOtherProduct(productForm.getSku(), productId)) {
            bindingResult.rejectValue("sku", "sku.exists", "Ya existe otro producto con esta referencia");
        }
    }
}
