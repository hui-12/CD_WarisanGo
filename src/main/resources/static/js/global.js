document.addEventListener('click', event => {
    const button = event.target.closest('[data-navigate]');
    if (!button) return;
    const routes = {home:'/home',directory:'/directory',map:'/map',saved:'/saved',rewards:'/rewards',badges:'/badges',challenges:'/challenges',profile:'/profile',admin:'/ai-discovery','ai-discovery':'/ai-discovery','pending-list':'/admin/pending','audit-log':'/admin/audit-log','admin-challenges':'/admin/challenges'};
    const route = routes[button.dataset.navigate];
    if (route) window.location.href = route;
});

window.escapeHtml = text => text == null ? '' : String(text).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;');

const initializeGuestGate = () => {
    if (window.warisanGoGuestGateInitialized) return;
    const guestGate = document.getElementById('guest-gate-modal');
    if (!guestGate) return;
    window.warisanGoGuestGateInitialized = true;

    document.addEventListener('click', event => {
        const action = event.target.closest(
            '.save-btn, [data-save-business], .map-saved-indicator, [data-check-in-business], '
            + '.btn-popup-check-in, [data-challenge-action], [data-guest-gated], '
            + '.review-primary-link, .review-secondary-link'
        );
        if (!action) return;
        event.preventDefault();
        event.stopImmediatePropagation();
        guestGate.showModal();
    }, true);
    guestGate.querySelector('.guest-gate-close')?.addEventListener('click', () => guestGate.close());
    guestGate.addEventListener('click', event => { if (event.target === guestGate) guestGate.close(); });
};

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeGuestGate, { once: true });
} else {
    initializeGuestGate();
}
