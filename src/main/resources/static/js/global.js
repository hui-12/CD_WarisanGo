/* --- Global Application Logic --- */

// Shared navigation handler
document.addEventListener("click", (event) => {
    const navigateBtn = event.target.closest("[data-navigate]");
    if (navigateBtn) {
        const target = navigateBtn.getAttribute("data-navigate");
        
        const routes = {
            "home": "/home",
            "directory": "/directory",
            "map": "/map",
            "saved": "/saved",
            "rewards": "/rewards",
            "badges": "/badges",
            "challenges": "/challenges",
            "ai-discovery": "/ai-discovery",
            "pending-list": "/admin/pending",
            "audit-log": "/admin/audit-log",
            "admin-challenges": "/admin/challenges",
            "profile": "/profile"
        };
        
        if (routes[target]) {
            window.location.href = routes[target];
        }
    }
});

// Shared HTML escaping utility to prevent XSS
window.escapeHtml = function (text) {
    if (text === null || text === undefined) {
        return '';
    }

    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};
/*const escapeHtml = (text) => {
    if (!text) return '';
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};*/
