document.addEventListener('DOMContentLoaded', () => {
  const forms = [...document.querySelectorAll('form[data-confirm-message]')];
  if (forms.length === 0) return;

  const dialog = document.createElement('dialog');
  dialog.className = 'admin-confirm-dialog';
  dialog.setAttribute('aria-labelledby', 'admin-confirm-title');
  dialog.innerHTML = `
    <div class="admin-confirm-dialog-content">
      <p class="module-eyebrow">Confirm action</p>
      <h2 id="admin-confirm-title">Are you sure?</h2>
      <p data-confirm-message></p>
      <div class="admin-confirm-actions">
        <button type="button" data-confirm-cancel>Cancel</button>
        <button class="admin-confirm-submit" type="button" data-confirm-submit>Confirm</button>
      </div>
    </div>`;
  document.body.append(dialog);

  const message = dialog.querySelector('[data-confirm-message]');
  const confirmButton = dialog.querySelector('[data-confirm-submit]');
  const cancelButton = dialog.querySelector('[data-confirm-cancel]');
  let pendingForm = null;

  forms.forEach((form) => {
    form.addEventListener('submit', (event) => {
      event.preventDefault();
      pendingForm = form;
      message.textContent = form.dataset.confirmMessage;
      confirmButton.textContent = form.dataset.confirmLabel || 'Confirm';
      confirmButton.classList.toggle('is-danger', form.dataset.confirmDanger === 'true');
      dialog.showModal();
    });
  });

  cancelButton.addEventListener('click', () => {
    pendingForm = null;
    dialog.close();
  });

  confirmButton.addEventListener('click', () => {
    const form = pendingForm;
    pendingForm = null;
    dialog.close();
    form?.submit();
  });

  dialog.addEventListener('click', (event) => {
    if (event.target === dialog) {
      pendingForm = null;
      dialog.close();
    }
  });
});
