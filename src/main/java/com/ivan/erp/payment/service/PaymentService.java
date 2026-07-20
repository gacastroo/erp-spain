package com.ivan.erp.payment.service;

import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.Payment;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.payment.PaymentSummary;
import com.ivan.erp.payment.web.PaymentForm;
import com.ivan.erp.payment.web.PaymentInvoiceOption;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        if (outstanding.signum() < 0) {
            outstanding = zeroMoney();
        }
        return new PaymentSummary(paid, outstanding);
    }

    @Transactional(readOnly = true)
    public List<PaymentInvoiceOption> getPayableInvoiceOptions() {
        return invoiceRepository.findByStatusNotInOrderByIssueDateDescIdDesc(NOT_PAYABLE_STATUSES)
                .stream()
                .map(this::buildInvoiceOption)
                .filter(option -> option.getOutstanding().signum() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentInvoiceOption getPayableInvoiceOption(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithClientQuoteAndLines(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        validateInvoiceCanReceivePayment(invoice);
        PaymentInvoiceOption option = buildInvoiceOption(invoice);
        if (option.getOutstanding().signum() <= 0) {
            throw new IllegalStateException("Esta factura ya está completamente cobrada");
        }
        return option;
    }

    @Transactional
    public Payment create(PaymentForm form) {
        validatePaymentDate(form.getPaymentDate());
        BigDecimal amount = normalizeMoney(form.getAmount());
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("El importe del cobro debe ser mayor que cero");
        }

        Invoice invoice = invoiceRepository.findByIdForUpdate(form.getInvoiceId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        validateInvoiceCanReceivePayment(invoice);

        BigDecimal paidBefore = normalizeMoney(paymentRepository.sumAmountByInvoiceId(invoice.getId()));
        BigDecimal outstanding = normalizeMoney(invoice.getTotal().subtract(paidBefore));
        if (outstanding.signum() <= 0) {
            throw new IllegalStateException("Esta factura ya está completamente cobrada");
        }
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("El importe del cobro supera el pendiente de la factura");
        }

        Payment savedPayment = paymentRepository.save(new Payment(
                invoice,
                form.getPaymentDate(),
                amount,
                form.getMethod(),
                form.getReference(),
                form.getNotes()
        ));
        paymentRepository.flush();
        refreshInvoicePaymentState(invoice);
        return savedPayment;
    }

    @Transactional
    public void delete(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Cobro no encontrado"));
        Invoice invoice = invoiceRepository.findByIdForUpdate(payment.getInvoice().getId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        paymentRepository.delete(payment);
        paymentRepository.flush();
        refreshInvoicePaymentState(invoice);
    }

    private void refreshInvoicePaymentState(Invoice invoice) {
        BigDecimal paidAmount = normalizeMoney(paymentRepository.sumAmountByInvoiceId(invoice.getId()));
        if (paidAmount.compareTo(invoice.getTotal()) >= 0) {
            LocalDate paidAt = paymentRepository.findLatestPaymentDateByInvoiceId(invoice.getId());
            if (paidAt == null) {
                throw new IllegalStateException("No se pudo determinar la fecha de cobro de la factura");
            }
            invoice.markPaid(paidAt);
        } else {
            invoice.markPendingAfterPaymentChange(LocalDate.now());
        }
    }

    private void validatePaymentDate(LocalDate paymentDate) {
        if (paymentDate == null) {
            throw new IllegalArgumentException("La fecha de cobro es obligatoria");
        }
        if (paymentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de cobro no puede estar en el futuro");
        }
    }

    private void validateInvoiceCanReceivePayment(Invoice invoice) {
        if (!invoice.canRegisterPayment()) {
            throw new IllegalStateException("Solo puedes registrar cobros en facturas emitidas, enviadas o vencidas");
        }
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
        return value == null ? zeroMoney() : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeQuery(String query) {
        return query == null || query.trim().isBlank() ? null : query.trim();
    }
}
