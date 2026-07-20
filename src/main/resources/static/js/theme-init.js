(function () {
  try {
    var saved = localStorage.getItem("erp-spain-theme-v6");
    var theme = saved === "dark" || saved === "light"
      ? saved
      : (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    document.documentElement.setAttribute("data-theme", theme);
    document.documentElement.setAttribute("data-bs-theme", theme);
    document.documentElement.style.colorScheme = theme;
  } catch (error) {
    document.documentElement.setAttribute("data-theme", "light");
    document.documentElement.setAttribute("data-bs-theme", "light");
  }
})();
