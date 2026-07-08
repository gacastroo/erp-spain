(() => {
    const REFRESH_INTERVAL_MS = 15000;

    let timerId = null;
    let loading = false;

    function currentRegion() {
        return document.getElementById('dashboard-live-region');
    }

    async function refreshDashboard() {
        const region = currentRegion();
        if (!region || loading || document.hidden) {
            return;
        }

        const liveUrl = region.dataset.liveUrl || '/dashboard/live';
        loading = true;
        region.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(liveUrl, {
                method: 'GET',
                credentials: 'same-origin',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                cache: 'no-store'
            });

            if (!response.ok) {
                return;
            }

            const html = await response.text();
            const doc = new DOMParser().parseFromString(html, 'text/html');
            const replacement = doc.querySelector('#dashboard-live-region');

            if (replacement) {
                region.replaceWith(replacement);
            }
        } catch (error) {
            // El dashboard debe seguir siendo usable aunque falle una actualización puntual.
        } finally {
            const newRegion = currentRegion();
            if (newRegion) {
                newRegion.setAttribute('aria-busy', 'false');
            }
            loading = false;
        }
    }

    function startTimer() {
        stopTimer();
        timerId = window.setInterval(refreshDashboard, REFRESH_INTERVAL_MS);
    }

    function stopTimer() {
        if (timerId) {
            window.clearInterval(timerId);
            timerId = null;
        }
    }

    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            stopTimer();
            return;
        }

        refreshDashboard();
        startTimer();
    });

    window.addEventListener('pageshow', () => {
        refreshDashboard();
        startTimer();
    });

    startTimer();
})();
