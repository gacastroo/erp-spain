package com.ivan.erp.client.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.client.web.ClientForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final String CLIENT_IN_USE_MESSAGE =
            "No se puede eliminar este cliente porque tiene facturas o presupuestos relacionados. Puedes desactivarlo.";

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Page<Client> search(String query, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "legalName")
        );

        return clientRepository.search(normalizeSearch(query), pageable);
    }

    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
    }

    @Transactional
    public Client create(ClientForm form) {
        String normalizedTaxId = normalizeTaxId(form.getTaxId());

        if (clientRepository.existsByTaxIdIgnoreCase(normalizedTaxId)) {
            throw new DataIntegrityViolationException("Ya existe un cliente con ese NIF/CIF/NIE");
        }

        Client client = new Client(form.getLegalName(), normalizedTaxId, form.getClientType());
        client.update(
                form.getLegalName(),
                form.getCommercialName(),
                normalizedTaxId,
                form.getEmail(),
                form.getPhone(),
                form.getAddressLine(),
                form.getCity(),
                form.getPostalCode(),
                form.getProvince(),
                form.getCountry(),
                form.getClientType(),
                form.getNotes()
        );

        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, ClientForm form) {
        Client client = getById(id);
        String normalizedTaxId = normalizeTaxId(form.getTaxId());

        if (clientRepository.existsByTaxIdIgnoreCaseAndIdNot(normalizedTaxId, id)) {
            throw new DataIntegrityViolationException("Ya existe otro cliente con ese NIF/CIF/NIE");
        }

        client.update(
                form.getLegalName(),
                form.getCommercialName(),
                normalizedTaxId,
                form.getEmail(),
                form.getPhone(),
                form.getAddressLine(),
                form.getCity(),
                form.getPostalCode(),
                form.getProvince(),
                form.getCountry(),
                form.getClientType(),
                form.getNotes()
        );

        return client;
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        Client client = getById(id);

        if (enabled) {
            client.activate();
        } else {
            client.deactivate();
        }
    }

    @Transactional
    public void delete(Long id) {
        Client client = getById(id);

        boolean hasInvoices = clientRepository.countInvoicesByClientId(id) > 0;
        boolean hasQuotes = clientRepository.countQuotesByClientId(id) > 0;

        if (hasInvoices || hasQuotes) {
            throw new DataIntegrityViolationException(CLIENT_IN_USE_MESSAGE);
        }

        clientRepository.delete(client);
    }

    private String normalizeSearch(String query) {
        if (query == null || query.trim().isBlank()) {
            return "";
        }
        return query.trim();
    }

    private String normalizeTaxId(String taxId) {
        if (taxId == null) {
            return null;
        }
        return taxId.trim().toUpperCase().replace(" ", "");
    }
}
