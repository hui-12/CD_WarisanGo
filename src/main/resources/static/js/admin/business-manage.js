document.addEventListener('DOMContentLoaded', () => {
  document.querySelector('[data-pinned-photo="true"]')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
});
