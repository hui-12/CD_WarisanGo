/* --- Global Application Logic --- */

// Shared navigation handler
function navigateTo(page) {
    if (page === 'map') {
        window.location.href = '/map';
    } else if (page === 'home') {
        window.location.href = '/';
    }
}

// Shared HTML escaping utility to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}