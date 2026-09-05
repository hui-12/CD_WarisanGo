const list = document.getElementById('badge-list');
const dialog = document.getElementById('badge-dialog');
const form = document.getElementById('badge-form');
const message = document.getElementById('badge-message');
const deleteDialog = document.getElementById('badge-delete-dialog');
const deleteBadgeName = document.getElementById('badge-delete-name');
let badges = [];
let pendingDeleteBadgeId = null;
const badgeCriteriaType = document.getElementById('badge-criteria-type');
const badgeTarget = document.getElementById('badge-target');

function updateTargetLimit() {
  const maximum = badgeCriteriaType.value === 'pointsEarned' ? 10000 : 300;
  badgeTarget.max = String(maximum);
  if (Number(badgeTarget.value) > maximum) badgeTarget.value = String(maximum);
}

const escapeHtml = (value) =>
  String(value ?? '').replace(
    /[&<>'"]/g,
    (character) =>
      ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;',
      })[character]
  );

async function loadBadges() {
  try {
    const response = await fetch('/api/admin/badges');
    if (!response.ok) throw new Error('Unable to load badges.');
    badges = await response.json();
    renderBadges();
  } catch (error) {
    list.innerHTML = '<p class="badge-empty">Unable to load badges from Firebase.</p>';
  }
}

function renderBadges() {
  if (!badges.length) {
    list.innerHTML = '<p class="badge-empty">No badges yet. Create the first badge.</p>';
    return;
  }
  list.innerHTML = badges
    .map(
      (badge) => `<article class="admin-badge-card">
    <span class="admin-badge-emoji">${escapeHtml(badge.emoji)}</span>
    <h2>${escapeHtml(badge.name)}</h2>
    <p>${escapeHtml(badge.description)}</p>
    <span class="admin-badge-rule">${escapeHtml(badge.unlockCriteria)} · Target ${Number(badge.target)}</span>
    <div class="admin-badge-actions">
      <button class="edit-badge" type="button" data-edit="${escapeHtml(badge.badgeId)}">Edit</button>
      <button class="delete-badge" type="button" data-delete="${escapeHtml(badge.badgeId)}">Delete</button>
    </div>
  </article>`
    )
    .join('');
}

function openForm(badge = null) {
  form.reset();
  document.getElementById('badge-id').value = badge?.badgeId ?? '';
  document.getElementById('dialog-title').textContent = badge ? 'Edit Badge' : 'Create Badge';
  document.getElementById('badge-name').value = badge?.name ?? '';
  document.getElementById('badge-emoji').value = badge?.emoji ?? '';
  document.getElementById('badge-description').value = badge?.description ?? '';
  document.getElementById('badge-unlock-criteria').value = badge?.unlockCriteria ?? '';
  document.getElementById('badge-criteria-type').value = badge?.criteriaType ?? 'checkInCount';
  document.getElementById('badge-target').value = badge?.target ?? 1;
  updateTargetLimit();
  dialog.showModal();
}

function showMessage(text, error = false) {
  message.textContent = text;
  message.classList.toggle('error', error);
  message.hidden = false;
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const badgeId = document.getElementById('badge-id').value;
  const body = {
    name: document.getElementById('badge-name').value.trim(),
    emoji: document.getElementById('badge-emoji').value.trim(),
    description: document.getElementById('badge-description').value.trim(),
    unlockCriteria: document.getElementById('badge-unlock-criteria').value.trim(),
    criteriaType: document.getElementById('badge-criteria-type').value,
    target: Number(document.getElementById('badge-target').value),
  };
  const response = await fetch(badgeId ? `/api/admin/badges/${encodeURIComponent(badgeId)}` : '/api/admin/badges', {
    method: badgeId ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    showMessage(result.message || 'Unable to save badge.', true);
    return;
  }
  dialog.close();
  showMessage(`Badge ${badgeId ? 'updated' : 'created'} successfully.`);
  await loadBadges();
});

list.addEventListener('click', (event) => {
  const editButton = event.target.closest('[data-edit]');
  if (editButton) {
    openForm(badges.find((badge) => badge.badgeId === editButton.dataset.edit));
    return;
  }
  const deleteButton = event.target.closest('[data-delete]');
  if (!deleteButton) return;
  const badge = badges.find((item) => item.badgeId === deleteButton.dataset.delete);
  pendingDeleteBadgeId = deleteButton.dataset.delete;
  deleteBadgeName.textContent = badge?.name || 'this badge';
  deleteDialog.showModal();
});

async function deletePendingBadge() {
  if (!pendingDeleteBadgeId) return;
  const badgeId = pendingDeleteBadgeId;
  const response = await fetch(`/api/admin/badges/${encodeURIComponent(badgeId)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    deleteDialog.close();
    pendingDeleteBadgeId = null;
    showMessage('Unable to delete badge.', true);
    return;
  }
  deleteDialog.close();
  pendingDeleteBadgeId = null;
  showMessage('Badge deleted successfully.');
  await loadBadges();
}

document.getElementById('create-badge').addEventListener('click', () => openForm());
badgeCriteriaType.addEventListener('change', updateTargetLimit);
document.getElementById('close-dialog').addEventListener('click', () => dialog.close());
document.getElementById('cancel-badge').addEventListener('click', () => dialog.close());
document.getElementById('confirm-badge-delete').addEventListener('click', deletePendingBadge);
document.getElementById('cancel-badge-delete').addEventListener('click', () => {
  pendingDeleteBadgeId = null;
  deleteDialog.close();
});
loadBadges();
