/* --- Global Application Logic --- */

// Shared navigation handler
document.addEventListener("click", function (event) {
    const navigateBtn = event.target.closest("[data-navigate]");
    if (navigateBtn) {
        const target = navigateBtn.getAttribute("data-navigate");
        // Map the data-navigate values to your actual URL endpoints
        const routes = {
            "home": "/",
            "directory": "/directory",
            "map": "/map",
            "rewards": "/rewards",
            "badges": "/badges",
            "challenges": "/challenges",
            "profile": "/profile"
        };
        
        if (routes[target]) {
            window.location.href = routes[target];
        }
    }
});

// Shared HTML escaping utility to prevent XSS
const escapeHtml = (text) => {
    if (!text) return '';
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};