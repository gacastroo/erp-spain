(function () {
    function parseNumber(value) {
        if (!value) {
            return 0;
        }
        return Number(String(value).replace(',', '.')) || 0;
    }

    function formatMoney(value) {
        return value.toLocaleString('es-ES', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function updateTotals() {
        const baseInput = document.getElementById('baseAmount');
        const vatInput = document.getElementById('vatRate');
        const vatPreview = document.getElementById('vatPreview');
        const totalPreview = document.getElementById('totalPreview');

        if (!baseInput || !vatInput || !vatPreview || !totalPreview) {
            return;
        }

        const base = parseNumber(baseInput.value);
        const vatRate = parseNumber(vatInput.value);
        const vatAmount = base * vatRate / 100;
        const total = base + vatAmount;

        vatPreview.textContent = formatMoney(vatAmount) + ' €';
        totalPreview.textContent = formatMoney(total) + ' €';
    }

    document.addEventListener('input', function (event) {
        if (event.target && (event.target.id === 'baseAmount' || event.target.id === 'vatRate')) {
            updateTotals();
        }
    });

    document.addEventListener('DOMContentLoaded', updateTotals);
})();
