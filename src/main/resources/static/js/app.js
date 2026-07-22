/* ERP Spain — comportamiento global, navegación, tema y estados. */
(() => {
  "use strict";

  const STORAGE_KEY = "erp-spain-theme-v6";
  const root = document.documentElement;

  const svg = (content, viewBox = "0 0 24 24") =>
    `<svg aria-hidden="true" viewBox="${viewBox}">${content}</svg>`;

  const ICONS = {
    logo: svg(
      '<path d="M5 19V11"/><path d="M12 19V5"/><path d="M19 19V8"/><path d="M3 19h18"/>'
    ),
    moon: svg(
      '<path d="M20.6 15.4A8 8 0 0 1 8.6 3.4 8.5 8.5 0 1 0 20.6 15.4Z"/>'
    ),
    sun: svg(
      '<circle cx="12" cy="12" r="3.5"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.65 17.65l1.42 1.42M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.65 6.35l1.42-1.42"/>'
    ),
    dashboard: svg(
      '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>'
    ),
    clients: svg(
      '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>'
    ),
    products: svg(
      '<path d="m21 8-9-5-9 5 9 5 9-5Z"/><path d="m3 8 9 5 9-5"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/>'
    ),
    quotes: svg(
      '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6M8 13h8M8 17h5"/>'
    ),
    invoices: svg(
      '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v6h6M8 13h8M8 17h8M8 9h2"/>'
    ),
    payments: svg(
      '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20M6 15h4"/>'
    ),
    expenses: svg(
      '<path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7H14a3.5 3.5 0 0 1 0 7H6"/>'
    ),
    reports: svg(
      '<path d="M3 3v18h18"/><path d="m7 16 4-5 4 3 5-7"/>'
    ),
    taxes: svg(
      '<path d="M19 5 5 19"/><circle cx="7" cy="7" r="3"/><circle cx="17" cy="17" r="3"/>'
    ),
    settings: svg(
      '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.09A1.7 1.7 0 0 0 9 19.35a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.63 15 1.7 1.7 0 0 0 3.07 14H3v-4h.09A1.7 1.7 0 0 0 4.65 9a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.63h.02A1.7 1.7 0 0 0 10 3.07V3h4v.09A1.7 1.7 0 0 0 15 4.65a1.7 1.7 0 0 0 1.88-.34l.06-.06 2.83 2.83-.06.06A1.7 1.7 0 0 0 19.37 9v.02A1.7 1.7 0 0 0 20.93 10H21v4h-.09A1.7 1.7 0 0 0 19.4 15Z"/>'
    ),
    default: svg('<circle cx="12" cy="12" r="8"/><path d="M8 12h8"/>')
  };

  const GROUPS = [
    { name: "General", keys: ["dashboard"] },
    { name: "Operaciones", keys: ["clients", "products", "quotes", "invoices"] },
    { name: "Gestión financiera", keys: ["payments", "expenses"] },
    { name: "Análisis y fiscalidad", keys: ["reports", "taxes"] },
    { name: "Sistema", keys: ["settings"] }
  ];

  function preferredTheme() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === "light" || saved === "dark") return saved;
    return window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }

  function currentTheme() {
    return root.dataset.theme === "dark" ? "dark" : "light";
  }

  function applyTheme(theme, persist = false) {
    const safeTheme = theme === "dark" ? "dark" : "light";
    root.dataset.theme = safeTheme;
    root.setAttribute("data-bs-theme", safeTheme);
    root.style.colorScheme = safeTheme;

    if (persist) localStorage.setItem(STORAGE_KEY, safeTheme);
    updateThemeButtons();
  }

  function updateThemeButtons() {
    const dark = currentTheme() === "dark";
    document.querySelectorAll("[data-erp-v6-theme-toggle]").forEach((button) => {
      button.setAttribute("aria-pressed", String(dark));
      button.setAttribute("aria-label", dark ? "Activar modo claro" : "Activar modo oscuro");
      button.title = dark ? "Modo claro" : "Modo oscuro";
      button.innerHTML = `${dark ? ICONS.sun : ICONS.moon}<span>${dark ? "Modo claro" : "Modo oscuro"}</span>`;
    });
  }

  function createThemeButton(extraClass = "") {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `erp-v6-theme-toggle ${extraClass}`.trim();
    button.dataset.erpV6ThemeToggle = "";
    button.addEventListener("click", () => {
      applyTheme(currentTheme() === "dark" ? "light" : "dark", true);
    });
    return button;
  }

  function installThemeButton() {
    document.querySelectorAll("[data-erp-v6-theme-toggle]").forEach((node, index) => {
      if (index > 0) node.remove();
    });

        /* ERP_THEME_SKIP_LOGIN */
    if (
      document.querySelector(".auth-card") ||
      document.body?.classList.contains("auth-page")
    ) {
      updateThemeButtons();
      return;
    }
const header = document.querySelector(".app-page > header.navbar, header.navbar, .navbar.sticky-top");
    if (header) {
      const existing = header.querySelector("[data-erp-v6-theme-toggle]");
      if (!existing) {
        const button = createThemeButton();
        const rightArea =
          header.querySelector(".ms-auto.d-flex, .d-flex.ms-auto, .navbar-nav.ms-auto") ||
          header.querySelector(".container-fluid") ||
          header;
        button.classList.add("ms-auto");
        rightArea.appendChild(button);
      }
    }

    updateThemeButtons();
  }

  function installBrand() {
    document.querySelectorAll(".navbar-brand").forEach((brand) => {
      brand.classList.add("erp-v6-brand");
      brand.innerHTML = `
        <span class="erp-v6-mark">${ICONS.logo}</span>
        <span class="erp-v6-wordmark">
          <strong>ERP Spain</strong>
          <small>Gestión financiera</small>
        </span>`;
    });

    document.querySelectorAll(".erp-ledger-mark, .erp-workspace-label, .online-status, .environment-status").forEach((node) => {
      if (!node.closest(".navbar-brand")) node.remove();
    });

    document.querySelectorAll("header span, header small, header div").forEach((node) => {
      if (node.children.length > 0) return;
      const text = (node.textContent || "").trim().toLowerCase();
      if (/^(online|activo|entorno de empresa|entorno activo)$/.test(text)) node.remove();
    });
  }

  function cleanPath(link) {
    try {
      return new URL(link.href, window.location.origin).pathname.toLowerCase();
    } catch {
      return (link.getAttribute("href") || "").toLowerCase();
    }
  }

  function navKey(link) {
    const path = cleanPath(link);
    const text = (link.textContent || "").trim().toLowerCase();

    if (path.includes("/dashboard") || text.includes("dashboard") || text === "inicio") return "dashboard";
    if (path.includes("/clients") || text.includes("cliente")) return "clients";
    if (path.includes("/products") || text.includes("producto") || text.includes("servicio")) return "products";
    if (path.includes("/quotes") || text.includes("presupuesto")) return "quotes";
    if (path.includes("/invoices") || text.includes("factura")) return "invoices";
    if (path.includes("/payments") || text.includes("cobro") || text.includes("pago")) return "payments";
    if (path.includes("/expenses") || text.includes("gasto")) return "expenses";
    if (path.includes("/reports") || text.includes("reporte") || text.includes("informe")) return "reports";
    if (path.includes("/taxes") || text.includes("impuesto") || text.includes("iva")) return "taxes";
    if (path.includes("/settings") || text.includes("configuración") || text.includes("empresa")) return "settings";
    return "default";
  }

  function groupNameFor(key) {
    return GROUPS.find((group) => group.keys.includes(key))?.name || "General";
  }

  function installNavigation(container) {
    container.querySelectorAll(".erp-v6-nav-section, .erp-nav-section").forEach((node) => node.remove());

    const links = Array.from(container.querySelectorAll("a.nav-link")).filter(
      (link, index, list) => list.indexOf(link) === index
    );

    const insertedGroups = new Set();

    links.forEach((link) => {
      const key = navKey(link);
      const groupName = groupNameFor(key);
      const host = link.parentElement?.tagName === "LI" ? link.parentElement : link;

      host.classList.add("erp-v6-nav-item");

      link.querySelectorAll(".erp-nav-icon, .erp-v6-nav-icon").forEach((icon) => icon.remove());
      const icon = document.createElement("span");
      icon.className = "erp-v6-nav-icon";
      icon.innerHTML = ICONS[key] || ICONS.default;
      link.prepend(icon);

      if (!insertedGroups.has(groupName) && host.parentElement === container) {
        const label = document.createElement("li");
        label.className = "erp-v6-nav-section";
        label.textContent = groupName;
        container.insertBefore(label, host);
        insertedGroups.add(groupName);
      }
    });
  }

  function installSidebar() {
    document.querySelectorAll(".app-sidebar .nav, .offcanvas .nav").forEach(installNavigation);
  }

  function normalizeSurfaces() {
    const selectors = [
      ".app-main .bg-white",
      ".app-main .bg-light",
      ".app-main .bg-body",
      ".app-main .bg-body-secondary",
      ".app-main .bg-body-tertiary",
      ".app-main .form-section",
      ".app-main .summary-box",
      ".app-main .quote-line-card",
      ".app-main .mobile-list-card",
      ".app-main .detail-card",
      ".app-main fieldset"
    ];

    document.querySelectorAll(selectors.join(",")).forEach((node) => {
      node.classList.add("erp-v6-force-surface");
    });

    document.querySelectorAll(".app-main [style]").forEach((node) => {
      const style = (node.getAttribute("style") || "").toLowerCase().replace(/\s+/g, "");
      if (
        style.includes("background:white") ||
        style.includes("background-color:white") ||
        style.includes("background:#fff") ||
        style.includes("background-color:#fff") ||
        style.includes("background:#ffffff") ||
        style.includes("background-color:#ffffff")
      ) {
        node.classList.add("erp-v6-force-surface");
      }
    });
  }

  function markPendingPanel() {
    document.querySelectorAll(".app-main h1, .app-main h2, .app-main h3, .app-main h4, .app-main h5").forEach((heading) => {
      if ((heading.textContent || "").trim().toLowerCase() !== "pendientes") return;
      const card = heading.closest(".card");
      if (card) card.classList.add("erp-v6-pending-card");
    });
  }

  function init() {
    root.classList.add("erp-v6");
    applyTheme(preferredTheme());
    installBrand();
    installThemeButton();
    installSidebar();
    normalizeSurfaces();
    markPendingPanel();
    document.body.classList.add("erp-v6-ready");
  }

  applyTheme(preferredTheme());

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init, { once: true });
  } else {
    init();
  }

  window.matchMedia?.("(prefers-color-scheme: dark)").addEventListener?.("change", (event) => {
    if (!localStorage.getItem(STORAGE_KEY)) applyTheme(event.matches ? "dark" : "light");
  });
})();

/* =========================================================
   Prisma V6.6 — detección real de formularios de estado
   No depende de que el título sea h1, h2, etc.
   ========================================================= */
(() => {
  "use strict";

  const STATE_DEFS = [
    { key: "borrador", labels: ["borrador"] },
    { key: "emitida", labels: ["emitida"] },
    { key: "enviada", labels: ["enviada"] },
    { key: "enviado", labels: ["enviado"] },
    { key: "vencida", labels: ["vencida"] },
    { key: "cancelada", labels: ["cancelada"] },
    { key: "cobrada", labels: ["cobrada"] },
    { key: "aceptado", labels: ["aceptado", "aceptada"] },
    { key: "rechazado", labels: ["rechazado", "rechazada"] },
    { key: "pagado", labels: ["pagado", "pagada"] }
  ];

  const normalize = (value) =>
    (value || "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/\s+/g, " ")
      .trim()
      .toLowerCase();

  function visibleLabel(element) {
    if (!element) return "";
    return normalize(
      element.textContent ||
      element.value ||
      element.getAttribute("value") ||
      element.getAttribute("aria-label") ||
      element.getAttribute("title") ||
      ""
    );
  }

  function stateFromText(value) {
    const text = normalize(value);
    if (!text) return "";

    for (const definition of STATE_DEFS) {
      if (definition.labels.some((label) => text === label || text.includes(label))) {
        return definition.key;
      }
    }
    return "";
  }

  function actionElements(scope = document) {
    return Array.from(scope.querySelectorAll(
      'button, input[type="submit"], input[type="button"], a.btn, a[role="button"]'
    )).filter((element) => stateFromText(visibleLabel(element)));
  }

  function hostFor(action) {
    return action.closest("form") || action;
  }

  function panelHasStateContext(panel) {
    const text = normalize(panel.textContent);
    return (
      text.includes("estado actual") ||
      text.includes("estado del presupuesto") ||
      text.includes("estado de la factura") ||
      text.includes("cobrada se aplica automaticamente") ||
      text.includes("cambiar estado")
    );
  }

  function findSmallestPanel(action) {
    let node = hostFor(action).parentElement;
    const main = action.closest(".app-main") || document.body;

    while (node && node !== main.parentElement) {
      const actions = actionElements(node);
      const distinctStates = new Set(actions.map((item) => stateFromText(visibleLabel(item))));

      if (actions.length >= 3 && distinctStates.size >= 3 && panelHasStateContext(node)) {
        return node;
      }

      if (node === main) break;
      node = node.parentElement;
    }
    return null;
  }

  function detectCurrentState(panel) {
    const candidates = Array.from(panel.querySelectorAll("p, div, span, small, strong"));

    for (const candidate of candidates) {
      const text = normalize(candidate.textContent);
      if (!text.includes("estado actual")) continue;

      const badge = candidate.querySelector(
        ".invoice-status, .quote-status, .payment-status, .badge, strong, .fw-semibold"
      );
      const badgeState = stateFromText(visibleLabel(badge));
      if (badgeState) return badgeState;

      const afterColon = text.split("estado actual").pop().replace(/^\s*:\s*/, "");
      const parsed = stateFromText(afterColon);
      if (parsed) return parsed;
    }

    return "";
  }

  function collectPanelActions(panel) {
    const seenHosts = new Set();
    const result = [];

    actionElements(panel).forEach((action) => {
      const state = stateFromText(visibleLabel(action));
      const host = hostFor(action);
      if (!state || seenHosts.has(host)) return;
      seenHosts.add(host);
      result.push({ action, host, state });
    });

    return result;
  }

  function rebuildPanel(panel) {
    if (!panel || panel.dataset.erpV66StatusDone === "true") return;

    const actions = collectPanelActions(panel);
    const uniqueStates = new Set(actions.map(({ state }) => state));
    if (actions.length < 3 || uniqueStates.size < 3) return;

    const firstHost = actions[0].host;
    const parent = firstHost.parentNode;
    if (!parent) return;

    const control = document.createElement("div");
    control.className = "erp-status-control";

    const title = document.createElement("div");
    title.className = "erp-status-control__title";
    title.textContent = "Seleccionar estado";

    const tabs = document.createElement("div");
    tabs.className = "erp-status-tabs";
    tabs.setAttribute("role", "group");
    tabs.setAttribute("aria-label", "Cambiar estado");

    control.append(title, tabs);
    parent.insertBefore(control, firstHost);

    const currentState = detectCurrentState(panel);

    actions.forEach(({ action, host, state }) => {
      host.classList.add("erp-status-tab-host");
      action.classList.add("erp-status-tab");
      action.dataset.erpState = state;

      const isCurrent = Boolean(currentState && state === currentState);
      action.classList.toggle("is-current-state", isCurrent);
      action.setAttribute("aria-pressed", String(isCurrent));
      if (isCurrent) action.setAttribute("aria-current", "true");
      else action.removeAttribute("aria-current");

      tabs.appendChild(host);
    });

    panel.dataset.erpV66StatusDone = "true";
  }

  function initializeStatusTabs() {
    const processedPanels = new Set();

    actionElements(document).forEach((action) => {
      const panel = findSmallestPanel(action);
      if (!panel || processedPanels.has(panel)) return;
      processedPanels.add(panel);
      rebuildPanel(panel);
    });
  }

  function scheduleInitialization() {
    initializeStatusTabs();
    window.setTimeout(initializeStatusTabs, 80);
    window.setTimeout(initializeStatusTabs, 350);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", scheduleInitialization, { once: true });
  } else {
    scheduleInitialization();
  }
})();

/* Prisma V6.13 — login estable sin mover la tarjeta del formulario. */
(() => {
  "use strict";

  const logoSvg = `
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 18V12M10 18V8M15 18V5M20 18V10" stroke-width="1.8" stroke-linecap="round"/>
      <path d="M3.5 19.5H21" stroke-width="1.8" stroke-linecap="round"/>
    </svg>`;

  function directChildContaining(ancestor, descendant) {
    let node = descendant;
    while (node && node.parentElement !== ancestor) node = node.parentElement;
    return node && node.parentElement === ancestor ? node : null;
  }

  function commonAncestor(a, b) {
    if (!a || !b) return null;
    const parents = new Set();
    let node = a;
    while (node) {
      parents.add(node);
      node = node.parentElement;
    }
    node = b;
    while (node) {
      if (parents.has(node)) return node;
      node = node.parentElement;
    }
    return null;
  }

  function removePresenceIndicators() {
    document.querySelectorAll(
      ".online-status, .environment-status, .active-indicator, .active-dot, .online-dot, " +
      ".status-dot, .presence-dot, [data-online-status], [data-active-indicator], .erp-workspace-label"
    ).forEach((node) => node.remove());

    document.querySelectorAll("header span, header small, header div, .auth-page span, .auth-page small").forEach((node) => {
      if (node.children.length > 0) return;
      const text = (node.textContent || "").trim().toLowerCase();
      if (/^(online|activo|activa|entorno activo|entorno de empresa)$/.test(text)) node.remove();
    });
  }

  function visualMarkup() {
    return `
      <div class="erp-login-stable-visual">
        <div class="erp-login-stable-brand">
          <span class="erp-login-stable-mark">${logoSvg}</span>
          <span class="erp-login-stable-wordmark">
            <strong>ERP Spain</strong>
            <small>Gestión financiera</small>
          </span>
        </div>

        <div class="erp-login-stable-copy">
          <span class="erp-login-stable-eyebrow">Gestión empresarial</span>
          <h1 class="erp-login-stable-title">Control financiero sin ruido.</h1>
          <p class="erp-login-stable-description">
            Facturación, presupuestos, cobros y gastos organizados en un único espacio de trabajo.
          </p>
          <div class="erp-login-stable-modules" aria-label="Módulos principales">
            <span>Facturas</span>
            <span>Presupuestos</span>
            <span>Cobros</span>
            <span>Gastos</span>
          </div>
        </div>

        <div class="erp-login-stable-footer">ERP Spain · Espacio privado de gestión</div>
      </div>`;
  }

  function installBrand(card) {
    if (card.querySelector(":scope > .erp-login-form-brand")) return;
    const brand = document.createElement("div");
    brand.className = "erp-login-form-brand";
    brand.innerHTML = `
      <span class="erp-login-form-brand__mark">${logoSvg}</span>
      <span class="erp-login-form-brand__text">
        <strong>ERP Spain</strong>
        <small>Acceso privado</small>
      </span>`;
    card.prepend(brand);
  }

  function installStableLogin() {
    const card = document.querySelector(".auth-card");
    const side = document.querySelector(".auth-side");
    if (!card) return;

    document.body.classList.add("erp-login-stable");
    removePresenceIndicators();

    let formPane = card.parentElement;
    let layout = null;

    if (side) {
      layout = commonAncestor(card, side);
      if (layout) {
        const candidate = directChildContaining(layout, card);
        if (candidate && candidate !== side) formPane = candidate;
        layout.classList.add("erp-login-layout");
      }
      side.innerHTML = visualMarkup();
      side.dataset.erpLoginStableVisual = "true";
    }

    if (formPane) {
      formPane.classList.add("erp-login-form-pane");
      /* No se llama a appendChild(card): la tarjeta conserva exactamente su lugar. */
    }

    installBrand(card);

    const form = card.querySelector("form");
    if (form) {
      form.classList.add("erp-login-form-stable");
      form.querySelectorAll(".form-control, input:not([type='checkbox']):not([type='radio']), textarea, select")
        .forEach((field) => field.classList.add("erp-login-field-stable"));
    }

    removePresenceIndicators();
  }

  function schedule() {
    installStableLogin();
    window.setTimeout(installStableLogin, 80);
    window.setTimeout(installStableLogin, 300);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", schedule, { once: true });
  } else {
    schedule();
  }
})();

/* Prisma V6.14: selector de tema fiable y exclusivo del login. */
(() => {
  "use strict";

  const STORAGE_KEY = "erp-spain-theme-v6";
  const root = document.documentElement;

  const sunIcon = `
    <svg aria-hidden="true" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="3.5"></circle>
      <path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.65 17.65l1.42 1.42M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.65 6.35l1.42-1.42"></path>
    </svg>`;

  const moonIcon = `
    <svg aria-hidden="true" viewBox="0 0 24 24">
      <path d="M20.6 15.4A8 8 0 0 1 8.6 3.4 8.5 8.5 0 1 0 20.6 15.4Z"></path>
    </svg>`;

  function currentTheme() {
    return root.dataset.theme === "dark" ? "dark" : "light";
  }

  function render(button) {
    const dark = currentTheme() === "dark";
    button.dataset.loginThemeCurrent = dark ? "dark" : "light";
    button.setAttribute("aria-pressed", String(dark));
    button.setAttribute("aria-label", dark ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    button.title = dark ? "Cambiar a modo claro" : "Cambiar a modo oscuro";
    button.innerHTML = `
      <span class="erp-login-theme-option erp-login-theme-option--light">
        ${sunIcon}<span>Claro</span>
      </span>
      <span class="erp-login-theme-option erp-login-theme-option--dark">
        ${moonIcon}<span>Oscuro</span>
      </span>`;
  }

  function syncLoginFieldTheme(theme) {
    const safeTheme = theme === "dark" ? "dark" : "light";
    const palette = safeTheme === "dark"
      ? { text: "#f4f6fb", background: "#151f2e" }
      : { text: "#182238", background: "#ffffff" };

    document.querySelectorAll("body.auth-page #username, body.auth-page #password")
      .forEach((field) => {
        /* Valores explícitos: Chromium no puede conservar los del tema anterior. */
        field.style.setProperty("color", palette.text, "important");
        field.style.setProperty("-webkit-text-fill-color", palette.text, "important");
        field.style.setProperty("caret-color", palette.text, "important");
        field.style.setProperty("background-color", palette.background, "important");
        field.style.setProperty("color-scheme", safeTheme);
      });
  }

  function applyTheme(theme, persist = true) {
    const safeTheme = theme === "dark" ? "dark" : "light";
    root.dataset.theme = safeTheme;
    root.setAttribute("data-bs-theme", safeTheme);
    root.style.colorScheme = safeTheme;
    if (persist) localStorage.setItem(STORAGE_KEY, safeTheme);
    document.querySelectorAll(".erp-login-theme-toggle").forEach(render);
    syncLoginFieldTheme(safeTheme);
    window.requestAnimationFrame(() => syncLoginFieldTheme(safeTheme));
  }

  function install() {
    const card = document.querySelector(".auth-card");
    if (!card) return;

    const pane = document.querySelector(".erp-login-form-pane") || card.parentElement;
    if (!pane) return;

    /* Retira duplicados y el botón flotante antiguo. */
    document.querySelectorAll("[data-erp-v6-theme-toggle], .erp-login-theme-toggle")
      .forEach((node) => node.remove());

    const button = document.createElement("button");
    button.type = "button";
    button.className = "erp-login-theme-toggle";
    button.setAttribute("role", "switch");
    button.addEventListener("click", () => {
      applyTheme(currentTheme() === "dark" ? "light" : "dark", true);
    });

    render(button);
    pane.appendChild(button);
    syncLoginFieldTheme(currentTheme());
  }

  function schedule() {
    install();
    window.setTimeout(install, 120);
    window.setTimeout(install, 420);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", schedule, { once: true });
  } else {
    schedule();
  }

  window.addEventListener("storage", (event) => {
    if (event.key === STORAGE_KEY && (event.newValue === "light" || event.newValue === "dark")) {
      applyTheme(event.newValue, false);
    }
  });
})();
