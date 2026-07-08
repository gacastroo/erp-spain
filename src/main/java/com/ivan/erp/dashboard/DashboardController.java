package com.ivan.erp.dashboard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        model.addAttribute("totalFacturado", "0,00 €");
        model.addAttribute("facturasPendientes", 0);
        model.addAttribute("facturasVencidas", 0);
        model.addAttribute("clientesActivos", 0);

        return "dashboard/index";
    }
}
