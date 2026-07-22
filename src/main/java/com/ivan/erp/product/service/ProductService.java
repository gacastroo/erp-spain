package com.ivan.erp.product.service;

import com.ivan.erp.product.Product;
import com.ivan.erp.product.ProductRepository;
import com.ivan.erp.product.web.ProductForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final int PAGE_SIZE = 10;
    private static final String PRODUCT_IN_USE_MESSAGE =
            "No se puede eliminar este producto porque está incluido en facturas o presupuestos. Puedes desactivarlo.";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String query, int page) {
        String normalizedQuery = normalizeQuery(query);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name")
        );

        return productRepository.search(normalizedQuery, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }

    @Transactional
    public Product create(ProductForm form) {
        Product product = new Product(
                form.getName(),
                form.getDescription(),
                form.getSku(),
                form.getProductType(),
                form.getUnitPrice(),
                form.getVatRate()
        );

        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = getById(id);

        product.update(
                form.getName(),
                form.getDescription(),
                form.getSku(),
                form.getProductType(),
                form.getUnitPrice(),
                form.getVatRate()
        );

        return product;
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        Product product = getById(id);

        if (enabled) {
            product.activate();
        } else {
            product.deactivate();
        }
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);

        boolean usedInInvoices = productRepository.countInvoiceLinesByProductId(id) > 0;
        boolean usedInQuotes = productRepository.countQuoteLinesByProductId(id) > 0;

        if (usedInInvoices || usedInQuotes) {
            throw new DataIntegrityViolationException(PRODUCT_IN_USE_MESSAGE);
        }

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public boolean skuExists(String sku) {
        if (sku == null || sku.trim().isBlank()) {
            return false;
        }

        return productRepository.existsBySkuIgnoreCase(sku.trim());
    }

    @Transactional(readOnly = true)
    public boolean skuExistsForOtherProduct(String sku, Long productId) {
        if (sku == null || sku.trim().isBlank()) {
            return false;
        }

        return productRepository.existsBySkuIgnoreCaseAndIdNot(sku.trim(), productId);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.trim().isBlank()) {
            return null;
        }

        return query.trim();
    }
}
