package com.ivan.erp.dashboard;

import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.PaymentRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class DashboardController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardController(
            ClientRepository clientRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            ExpenseRepository expenseRepository
    ) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @Transactional(readOnly = true)
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model, HttpServletResponse response) {
        preventBrowserCache(response);
        model.addAttribute("username", authentication.getName());
        addDashboardData(model);
        return "dashboard/index";
    }

    @Transactional(readOnly = true)
    @GetMapping("/dashboard/live")
    public String dashboardLive(Model model, HttpServletResponse response) {
        preventBrowserCache(response);
        addDashboardData(model);
        return "dashboard/index :: liveDashboard";
    }

    private void addDashboardData(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        int currentQuarter = ((today.getMonthValue() - 1) / 3) + 1;
        LocalDate startOfQuarter = LocalDate.of(today.getYear(), ((currentQuarter - 1) * 3) + 1, 1);
        LocalDate endOfQuarter = startOfQuarter.plusMonths(3).minusDays(1);

        List<InvoiceStatus> excludedInvoiceStatuses = List.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);
        List<InvoiceStatus> pendingInvoiceStatuses = List.of(InvoiceStatus.ISSUED, InvoiceStatus.SENT, InvoiceStatus.OVERDUE);

        BigDecimal invoicedThisMonth = safe(invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(
                startOfMonth,
                endOfMonth,
                excludedInvoiceStatuses
        ));
        BigDecimal collectedThisMonth = safe(paymentRepository.sumAmountByPaymentDateBetween(startOfMonth, endOfMonth));
        BigDecimal expensesThisMonth = safe(expenseRepository.sumTotalByExpenseDateBetween(startOfMonth, endOfMonth));
        BigDecimal cashResultThisMonth = collectedThisMonth.subtract(expensesThisMonth);

        BigDecimal issuedVatThisQuarter = safe(invoiceRepository.sumVatTotalByIssueDateBetweenAndStatusNotIn(
                startOfQuarter,
                endOfQuarter,
                excludedInvoiceStatuses
        ));
        BigDecimal deductibleVatThisQuarter = safe(expenseRepository.sumVatAmountByExpenseDateBetween(startOfQuarter, endOfQuarter));
        BigDecimal currentQuarterVat = issuedVatThisQuarter.subtract(deductibleVatThisQuarter);

        model.addAttribute("monthStart", startOfMonth);
        model.addAttribute("monthEnd", endOfMonth);
        model.addAttribute("invoicedThisMonth", invoicedThisMonth);
        model.addAttribute("collectedThisMonth", collectedThisMonth);
        model.addAttribute("expensesThisMonth", expensesThisMonth);
        model.addAttribute("cashResultThisMonth", cashResultThisMonth);
        model.addAttribute("pendingInvoices", invoiceRepository.countByStatusIn(pendingInvoiceStatuses));
        model.addAttribute("overdueInvoices", invoiceRepository.countOverdue(today, List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT)));
        model.addAttribute("unpaidExpenses", expenseRepository.countByPaidFalse());
        model.addAttribute("activeClients", clientRepository.countByEnabledTrue());
        model.addAttribute("currentQuarter", currentQuarter);
        model.addAttribute("currentQuarterVat", currentQuarterVat);
        model.addAttribute("currentQuarterVatAbs", currentQuarterVat.abs());
        model.addAttribute("currentQuarterVatToPay", currentQuarterVat.signum() >= 0);
        model.addAttribute("recentInvoices", invoiceRepository.findTop5ByOrderByIssueDateDescIdDesc());
        model.addAttribute("recentPayments", paymentRepository.findTop5ByOrderByPaymentDateDescIdDesc());
        model.addAttribute("recentExpenses", expenseRepository.findTop5ByOrderByExpenseDateDescIdDesc());
        model.addAttribute("dashboardUpdatedAt", LocalTime.now().format(TIME_FORMATTER));
    }

    private void preventBrowserCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
