package com.ivan.erp.dashboard;

import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.tax.TaxSummary;
import com.ivan.erp.tax.service.TaxService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
public class DashboardController {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final TaxService taxService;

    public DashboardController(
            ClientRepository clientRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            ExpenseRepository expenseRepository,
            TaxService taxService
    ) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.taxService = taxService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<InvoiceStatus> excludedInvoiceStatuses = List.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);
        List<InvoiceStatus> pendingInvoiceStatuses = List.of(InvoiceStatus.ISSUED, InvoiceStatus.SENT, InvoiceStatus.OVERDUE);

        BigDecimal invoicedThisMonth = invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(
                startOfMonth,
                endOfMonth,
                excludedInvoiceStatuses
        );
        BigDecimal collectedThisMonth = paymentRepository.sumAmountByPaymentDateBetween(startOfMonth, endOfMonth);
        BigDecimal expensesThisMonth = expenseRepository.sumTotalByExpenseDateBetween(startOfMonth, endOfMonth);
        BigDecimal cashResultThisMonth = safe(collectedThisMonth).subtract(safe(expensesThisMonth));
        int currentQuarter = ((today.getMonthValue() - 1) / 3) + 1;
        TaxSummary currentQuarterTaxes = taxService.buildQuarterSummary(today.getYear(), currentQuarter);

        model.addAttribute("username", authentication.getName());
        model.addAttribute("monthStart", startOfMonth);
        model.addAttribute("monthEnd", endOfMonth);
        model.addAttribute("invoicedThisMonth", safe(invoicedThisMonth));
        model.addAttribute("collectedThisMonth", safe(collectedThisMonth));
        model.addAttribute("expensesThisMonth", safe(expensesThisMonth));
        model.addAttribute("cashResultThisMonth", cashResultThisMonth);
        model.addAttribute("pendingInvoices", invoiceRepository.countByStatusIn(pendingInvoiceStatuses));
        model.addAttribute("overdueInvoices", invoiceRepository.countOverdue(today, List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT)));
        model.addAttribute("unpaidExpenses", expenseRepository.countByPaidFalse());
        model.addAttribute("activeClients", clientRepository.countByEnabledTrue());
        model.addAttribute("currentQuarter", currentQuarter);
        model.addAttribute("currentQuarterVat", currentQuarterTaxes.vatResult());
        model.addAttribute("currentQuarterVatAbs", currentQuarterTaxes.vatResultAbs());
        model.addAttribute("currentQuarterVatToPay", currentQuarterTaxes.isVatToPay());
        model.addAttribute("recentInvoices", invoiceRepository.findTop5ByOrderByIssueDateDescIdDesc());
        model.addAttribute("recentPayments", paymentRepository.findTop5ByOrderByPaymentDateDescIdDesc());
        model.addAttribute("recentExpenses", expenseRepository.findTop5ByOrderByExpenseDateDescIdDesc());

        return "dashboard/index";
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
