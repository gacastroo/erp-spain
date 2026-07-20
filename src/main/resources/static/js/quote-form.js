/**
 * Presupuestos: cálculo de totales en tiempo real.
 * Externalizado para cumplir la Content Security Policy sin permitir scripts inline.
 */
(() => {
    const linesContainer = document.getElementById('quoteLines');
    const addLineButton = document.getElementById('addLineButton');
    const subtotalPreview = document.getElementById('subtotalPreview');
    const vatPreview = document.getElementById('vatPreview');
    const totalPreview = document.getElementById('totalPreview');
    const issueDateInput = document.getElementById('issueDate');
    const validUntilInput = document.getElementById('validUntil');

    if (!linesContainer || !addLineButton) {
        return;
    }


    function syncDateRange() {
        if (!issueDateInput || !validUntilInput || !issueDateInput.value) {
            return;
        }

        validUntilInput.min = issueDateInput.value;

        if (validUntilInput.value && validUntilInput.value < issueDateInput.value) {
            validUntilInput.value = issueDateInput.value;
        }
    }

    function toNumber(value) {
        const parsed = Number(String(value || '0').replace(',', '.'));
        return Number.isFinite(parsed) ? parsed : 0;
    }

    function formatMoney(value) {
        return value.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function updateTotals() {
        let subtotal = 0;
        let vat = 0;

        linesContainer.querySelectorAll('.quote-line-card').forEach(card => {
            const quantity = toNumber(card.querySelector('.line-quantity')?.value);
            const price = toNumber(card.querySelector('.line-price')?.value);
            const vatRate = toNumber(card.querySelector('.line-vat')?.value);
            const lineSubtotal = quantity * price;
            const lineVat = lineSubtotal * vatRate / 100;

            subtotal += lineSubtotal;
            vat += lineVat;
        });

        subtotalPreview.textContent = formatMoney(subtotal);
        vatPreview.textContent = formatMoney(vat);
        totalPreview.textContent = formatMoney(subtotal + vat);
    }

    function renumberLines() {
        linesContainer.querySelectorAll('.quote-line-card').forEach((card, index) => {
            card.querySelector('.line-number').textContent = String(index + 1);

            card.querySelectorAll('[name]').forEach(field => {
                field.name = field.name.replace(/lines\[\d+]/, `lines[${index}]`);
            });

            card.querySelectorAll('[id]').forEach(field => {
                field.id = field.id.replace(/lines\d+\./, `lines${index}.`);
            });
        });
    }

    function bindLine(card) {
        card.querySelector('.product-select')?.addEventListener('change', event => {
            const selected = event.target.selectedOptions[0];
            const description = card.querySelector('.line-description');
            const price = card.querySelector('.line-price');
            const vat = card.querySelector('.line-vat');

            if (!selected || !selected.value) {
                return;
            }

            description.value = selected.dataset.description || selected.textContent.trim();
            price.value = selected.dataset.price || '0.00';
            vat.value = selected.dataset.vat || '21.00';
            updateTotals();
        });

        card.querySelectorAll('.line-quantity, .line-price, .line-vat').forEach(input => {
            input.addEventListener('input', updateTotals);
        });

        card.querySelector('.remove-line-button')?.addEventListener('click', () => {
            const cards = linesContainer.querySelectorAll('.quote-line-card');
            if (cards.length === 1) {
                card.querySelectorAll('input').forEach(input => input.value = input.classList.contains('line-quantity') ? '1' : '');
                card.querySelector('.line-price').value = '0.00';
                card.querySelector('.line-vat').value = '21.00';
                card.querySelector('.product-select').value = '';
                updateTotals();
                return;
            }

            card.remove();
            renumberLines();
            updateTotals();
        });
    }

    addLineButton.addEventListener('click', () => {
        const firstLine = linesContainer.querySelector('.quote-line-card');
        const clone = firstLine.cloneNode(true);

        clone.querySelectorAll('input').forEach(input => {
            input.classList.remove('is-invalid');
            input.value = input.classList.contains('line-quantity') ? '1' : '';
        });
        clone.querySelector('.line-price').value = '0.00';
        clone.querySelector('.line-vat').value = '21.00';
        clone.querySelector('.product-select').value = '';
        clone.querySelectorAll('.invalid-feedback').forEach(error => error.remove());

        linesContainer.appendChild(clone);
        renumberLines();
        bindLine(clone);
        updateTotals();
    });

    issueDateInput?.addEventListener('change', syncDateRange);
    syncDateRange();

    linesContainer.querySelectorAll('.quote-line-card').forEach(bindLine);
    renumberLines();
    updateTotals();
})();
