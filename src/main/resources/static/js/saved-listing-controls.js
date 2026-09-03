document.addEventListener('DOMContentLoaded', async () => {
  const buttons = [...document.querySelectorAll('[data-save-business]')];
  if (buttons.length === 0) return;

  const bookmarkSvg = `
    <svg viewBox="0 0 24 28" fill="none" stroke="currentColor" stroke-width="2"
         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M4 2h16v23l-8-5-8 5z"></path>
    </svg>`;

  const businessName = (button) => {
    const cardTitle = button.closest('.business-card')?.querySelector('h2')?.textContent;
    const detailTitle = document.getElementById('business-title')?.textContent;
    return (cardTitle || detailTitle || 'business').trim();
  };

  const updateButton = (button, saved) => {
    const name = businessName(button);
    button.setAttribute('aria-pressed', String(saved));
    button.setAttribute('aria-label', `${saved ? 'Remove' : 'Save'} ${name}`);
    button.querySelector('.bookmark-icon').innerHTML = bookmarkSvg;

    const text = button.querySelector('[data-save-label]');
    if (text) text.textContent = saved ? 'Saved' : 'Save';
  };

  buttons.forEach((button) => updateButton(button, false));

  if (document.getElementById('guest-gate-modal')) return;

  try {
    const response = await fetch('/api/saved-listings/ids', { headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error('Unable to load saved listings.');
    const savedIds = new Set(await response.json());
    buttons.forEach((button) => updateButton(button, savedIds.has(button.dataset.businessId)));
  } catch (error) {
    console.error(error);
  }

  buttons.forEach((button) => {
    button.addEventListener('click', async () => {
      const wasSaved = button.getAttribute('aria-pressed') === 'true';
      button.disabled = true;
      try {
        const response = await fetch(`/api/saved-listings/${encodeURIComponent(button.dataset.businessId)}`, {
          method: wasSaved ? 'DELETE' : 'POST',
          headers: { Accept: 'application/json' },
        });
        if (!response.ok) throw new Error('Unable to update saved listing.');
        updateButton(button, !wasSaved);
      } catch (error) {
        console.error(error);
      } finally {
        button.disabled = false;
      }
    });
  });
});
