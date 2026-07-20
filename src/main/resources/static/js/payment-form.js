/**
 * Cobros: mejora del formulario sin scripts inline para respetar CSP.
 */
(() => {
    const invoiceSelect = document.getElementById('invoiceId');
    const amountInput = document.getElementById('amount');
    const outstandingPreview = document.getElementById('outstandingPreview');

    if (!invoiceSelect || !amountInput) {
        return;
    }

    function formatMoney(value) {
        const number = Number(String(value || '0').replace(',', '.'));
        return Number.isFinite(number)
            ? number.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            : '0,00';
    }

    function syncAmountWithInvoice() {
        const selected = invoiceSelect.selectedOptions[0];
        if (!selected || !selected.value) {
            if (outstandingPreview) {
                outstandingPreview.textContent = 'Selecciona una factura para ver el pendiente.';
            }
            return;
        }

        const outstanding = selected.dataset.outstanding || '0.00';

        if (!amountInput.value || amountInput.value === '0' || amountInput.value === '0.00') {
            amountInput.value = Number(outstanding).toFixed(2);
        }

        if (outstandingPreview) {
            outstandingPreview.textContent = `Pendiente de cobrar: ${formatMoney(outstanding)} €`;
        }
    }

    invoiceSelect.addEventListener('change', () => {
        amountInput.value = '';
        syncAmountWithInvoice();
    });

    syncAmountWithInvoice();
})();
