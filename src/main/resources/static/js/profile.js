document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('[data-profile-tab]');
  const panels = document.querySelectorAll('[data-profile-panel]');
  tabs.forEach(tab => tab.addEventListener('click', () => {
    tabs.forEach(item => item.classList.toggle('active', item === tab));
    panels.forEach(panel => {
      const active = panel.dataset.profilePanel === tab.dataset.profileTab;
      panel.classList.toggle('active', active);
      panel.hidden = !active;
    });
  }));

  const form = document.querySelector('.profile-panel[method="post"]');
  const editButton = document.querySelector('[data-profile-edit]');
  const saveButton = document.querySelector('[data-profile-save]');
  const cancelButton = document.querySelector('[data-profile-cancel]');
  const editableFields = form ? Array.from(form.querySelectorAll('input, select, textarea')) : [];
  const initialValues = editableFields.map(field => field.value);

  const setEditing = enabled => {
    editableFields.forEach(field => {
      if (field instanceof HTMLSelectElement) field.disabled = !enabled;
      else field.readOnly = !enabled;
    });
    if (editButton) editButton.hidden = enabled;
    if (saveButton) saveButton.hidden = !enabled;
    if (cancelButton) cancelButton.hidden = !enabled;
  };
  editButton?.addEventListener('click', () => setEditing(true));
  cancelButton?.addEventListener('click', () => {
    editableFields.forEach((field, index) => { field.value = initialValues[index]; });
    setEditing(false);
  });

  document.getElementById('switch-account-button')?.addEventListener('click', async () => {
    try {
      const response = await fetch('/api/auth/logout', { method: 'POST' });
      if (!response.ok) throw new Error('Unable to end the current session.');
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout failed:', error);
      alert('Unable to log out. Please try again.');
    }
  });
});
