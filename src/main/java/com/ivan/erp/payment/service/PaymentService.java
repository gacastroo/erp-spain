package com.ivan.erp.payment.service;

import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.*;
import com.ivan.erp.payment.web.PaymentForm;
import com.ivan.erp.payment.web.PaymentInvoiceOption;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
public class PaymentService {

    private static final int PAGE_SIZE = 10;
    private static final Collection<InvoiceStatus> NOT_PAYABLE_STATUSES = List.of(
            InvoiceStatus.DRAFT,
            InvoiceStatus.PAID,
            InvoiceStatus.CANCELLED
    );

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public Page<Payment> search(String query, int page) {
        String normalizedQuery = normalizeQuery(query);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "paymentDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return paymentRepository.search(normalizedQuery, pageable);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoice_IdOrderByPaymentDateDescIdDesc(invoiceId);
    }

    @Transactional(readOnly = true)
    public PaymentSummary getSummaryForInvoice(Invoice invoice) {
        BigDecimal paid = normalizeMoney(paymentRepository.sumAmountByInvoiceId(invoice.getId()));
        BigDecimal outstanding = normalizeMoney(invoice.getTotal().subtract(paid));

        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new PaymentSummary(paid, outstanding);
    }

    @Transactional(readOnly = true)
    public List<PaymentInvoiceOption> getPayableInvoiceOptions() {
        return invoiceRepository.findByStatusNotInOrderByIssueDateDescIdDesc(NOT_PAYABLE_STATUSES)
                .stream()
                .map(this::buildInvoiceOption)
                .filter(option -> option.getOutstanding().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentInvoiceOption getPayableInvoiceOption(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithClientQuoteAndLines(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        validateInvoiceCanReceivePayment(invoice);

        PaymentInvoiceOption option = buildInvoiceOption(invoice);

        if (option.getOutstanding().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Esta factura ya está completamente cobrada");
        }

        return option;
    }

    @Transactional
    public Payment create(PaymentForm form) {
        Invoice invoice = invoiceRepository.findByIdWithClientQuoteAndLines(form.getInvoiceId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        validateInvoiceCanReceivePayment(invoice);

        BigDecimal amount = normalizeMoney(form.getAmount());
        BigDecimal paidBefore = normalizeMoney(paymentRepository.sumAmountByInvoiceId(invoice.getId()));
        BigDecimal outstanding = normalizeMoney(invoice.getTotal().subtract(paidBefore));

        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("El importe del cobro supera el pendiente de la factura");
        }

        Payment payment = new Payment(
                invoice,
                form.getPaymentDate(),
                amount,
                form.getMethod(),
                form.getReference(),
                form.getNotes()
        );

        Payment savedPayment = paymentRepository.save(payment);
        updateInvoiceStatusAfterPaymentChange(invoice, paidBefore.add(amount), form.getPaymentDate());

        return savedPayment;
    }

    @Transactional
    public void delete(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Cobro no encontrado"));

        Invoice invoice = payment.getInvoice();

        paymentRepository.delete(payment);
        paymentRepository.flush();

        BigDecimal paidAfterDelete = normalizeMoney(paymentRepository.sumAmountByInvoiceId(invoice.getId()));
        updateInvoiceStatusAfterPaymentChange(invoice, paidAfterDelete, LocalDate.now());
    }

    private void validateInvoiceCanReceivePayment(Invoice invoice) {
        if (!invoice.canRegisterPayment()) {
            throw new IllegalStateException("Solo puedes registrar cobros en facturas emitidas, enviadas o vencidas");
        }
    }

    private void updateInvoiceStatusAfterPaymentChange(Invoice invoice, BigDecimal paidAmount, LocalDate paymentDate) {
        BigDecimal normalizedPaid = normalizeMoney(paidAmount);

        if (normalizedPaid.compareTo(invoice.getTotal()) >= 0) {
            invoice.markPaid(paymentDate);
            return;
        }

        invoice.markPendingAfterPaymentChange(LocalDate.now());
    }

    private PaymentInvoiceOption buildInvoiceOption(Invoice invoice) {
        PaymentSummary summary = getSummaryForInvoice(invoice);

        return new PaymentInvoiceOption(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getClient().getLegalName(),
                invoice.getTotal(),
                summary.getPaidAmount(),
                summary.getOutstandingAmount()
        );
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.trim().isBlank()) {
            return null;
        }
        return query.trim();
    }
}
