package com.ivan.erp.dashboard;

import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
public class DashboardController {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardController(ClientRepository clientRepository, InvoiceRepository invoiceRepository) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal invoicedThisMonth = invoiceRepository.sumTotalByStatusAndIssueDateBetween(
                InvoiceStatus.PAID,
                startOfMonth,
                endOfMonth
        );

        model.addAttribute("username", authentication.getName());
        model.addAttribute("totalFacturado", formatCurrency(invoicedThisMonth));
        model.addAttribute("facturasPendientes", invoiceRepository.countByStatus(InvoiceStatus.ISSUED) + invoiceRepository.countByStatus(InvoiceStatus.SENT));
        model.addAttribute("facturasVencidas", invoiceRepository.countOverdue(today, List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED)));
        model.addAttribute("clientesActivos", clientRepository.countByEnabledTrue());

        return "dashboard/index";
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        return formatter.format(amount == null ? BigDecimal.ZERO : amount);
    }
}
