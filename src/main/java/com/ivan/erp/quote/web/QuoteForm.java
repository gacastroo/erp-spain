package com.ivan.erp.quote.web;

import com.ivan.erp.quote.Quote;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuoteForm {

    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate issueDate = LocalDate.now();

    private LocalDate validUntil = LocalDate.now().plusDays(30);

    @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
    private String notes;

    @Valid
    private List<QuoteLineForm> lines = new ArrayList<>();

    public QuoteForm() {
        lines.add(new QuoteLineForm());
    }

    public static QuoteForm fromQuote(Quote quote) {
        QuoteForm form = new QuoteForm();

        form.setClientId(quote.getClient().getId());
        form.setIssueDate(quote.getIssueDate());
        form.setValidUntil(quote.getValidUntil());
        form.setNotes(quote.getNotes());

        form.getLines().clear();
        quote.getLines().forEach(line -> form.getLines().add(QuoteLineForm.fromLine(line)));

        if (form.getLines().isEmpty()) {
            form.getLines().add(new QuoteLineForm());
        }

        return form;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = clean(notes);
    }

    public List<QuoteLineForm> getLines() {
        return lines;
    }

    public void setLines(List<QuoteLineForm> lines) {
        this.lines = lines;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
