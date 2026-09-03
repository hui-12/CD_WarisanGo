let adminChallenges = [];
let adminMode = 'list';
let editingId = null;
let deleteTargetId = null;

async function loadAdminChallenges() {
  try {
    const r = await fetch('/api/admin/challenges');
    if (!r.ok) throw new Error('Unable to load challenges');
    adminChallenges = await r.json();
    renderAdminRows();
  } catch (error) {
    console.error(error);
    document.getElementById('admin-rows').innerHTML =
      '<div class="view-admin-challenge-js-1">Unable to load challenges from Firebase.</div>';
  }
}

function renderAdminRows() {
  const container = document.getElementById('admin-rows');
  if (!adminChallenges.length) {
    container.innerHTML =
      '<div class="view-admin-challenge-js-2">No challenges yet. Create your first challenge.</div>';
    return;
  }
  container.innerHTML = adminChallenges
    .map((ch, i) => {
      const expired = ch.expiry && new Date(ch.expiry) < new Date();
      const encodedId = encodeURIComponent(ch.id).replace(/'/g, '%27');
      return `<div style="display:grid;grid-template-columns:1fr 80px 130px 110px 130px;padding:14px 20px;border-bottom:${i < adminChallenges.length - 1 ? '1px solid #e8e6e0' : 'none'};background:${expired ? '#f5e9c4' : i % 2 === 0 ? '#faf9f5' : '#fff'};align-items:center">
      <div><div class="view-admin-challenge-js-3">${escapeHtml(ch.title || '')}</div><div class="view-admin-challenge-js-4">${escapeHtml(ch.description || '')}</div><div style="font-size:9px;margin-top:4px;color:${String(ch.status).toUpperCase() === 'ACTIVE' ? '#2d7a4f' : '#9b9b98'}">${escapeHtml(ch.status || 'ACTIVE')}</div></div>
      <div class="view-admin-challenge-js-5">+${Number(ch.rewardPoints || 0)}</div>
      <div class="view-admin-challenge-js-6">${escapeHtml(ch.badge || '')}</div>
      <div class="view-admin-challenge-js-7">${escapeHtml(ch.expiry || '—')}</div>
      <div class="view-admin-challenge-js-8"><button type="button" data-admin-action="edit" data-challenge-id="${encodedId}" class="view-admin-challenge-js-9">Edit</button><button type="button" data-admin-action="delete" data-challenge-id="${encodedId}" class="view-admin-challenge-js-10">Delete</button></div>
    </div>`;
    })
    .join('');
}

function openAdminCreate() {
  adminMode = 'create';
  editingId = null;
  document.getElementById('admin-title').textContent = 'Create New Challenge';
  document.getElementById('admin-create-btn').style.display = 'none';
  document.getElementById('admin-list').style.display = 'none';
  document.getElementById('admin-form').style.display = 'block';
  document.getElementById('form-save-btn').textContent = 'Publish Challenge';
  ['title', 'desc', 'req', 'pts', 'badge', 'date'].forEach((k) => {
    document.getElementById('f-' + k).value = '';
  });
  document.getElementById('f-target').value = '1';
  document.getElementById('f-status').value = 'ACTIVE';
}

function openAdminEdit(id) {
  const ch = adminChallenges.find((c) => c.id === id);
  if (!ch) return;
  adminMode = 'edit';
  editingId = id;
  document.getElementById('admin-title').textContent = 'Edit: ' + ch.title;
  document.getElementById('admin-create-btn').style.display = 'none';
  document.getElementById('admin-list').style.display = 'none';
  document.getElementById('admin-form').style.display = 'block';
  document.getElementById('form-save-btn').textContent = 'Save Changes';
  document.getElementById('f-title').value = ch.title || '';
  document.getElementById('f-desc').value = ch.description || '';
  document.getElementById('f-req').value = ch.requirement || '';
  document.getElementById('f-target').value = ch.target || 1;
  document.getElementById('f-pts').value = ch.rewardPoints || 0;
  document.getElementById('f-badge').value = ch.badge || '';
  document.getElementById('f-date').value = ch.expiry || '';
  document.getElementById('f-status').value = String(ch.status || 'ACTIVE').toUpperCase();
}

function closeAdminForm() {
  adminMode = 'list';
  document.getElementById('admin-title').textContent = 'Challenge Management';
  document.getElementById('admin-create-btn').style.display = 'block';
  document.getElementById('admin-list').style.display = 'block';
  document.getElementById('admin-form').style.display = 'none';
}

async function saveAdminForm() {
  const body = {
    title: document.getElementById('f-title').value.trim(),
    description: document.getElementById('f-desc').value.trim(),
    requirement: document.getElementById('f-req').value.trim(),
    target: Number(document.getElementById('f-target').value),
    rewardPoints: Number(document.getElementById('f-pts').value),
    badge: document.getElementById('f-badge').value.trim(),
    expiry: document.getElementById('f-date').value,
    status: document.getElementById('f-status').value,
  };
  if (
    !body.title ||
    !body.description ||
    !body.requirement ||
    body.target < 1 ||
    body.rewardPoints < 1 ||
    !body.badge ||
    !body.expiry
  ) {
    alert('Please complete all fields with valid values.');
    return;
  }
  const operation = adminMode;
  const url =
    operation === 'create' ? '/api/admin/challenges' : `/api/admin/challenges/${encodeURIComponent(editingId)}`;
  const r = await fetch(url, {
    method: operation === 'create' ? 'POST' : 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!r.ok) {
    alert('Unable to save challenge.');
    return;
  }
  closeAdminForm();
  await loadAdminChallenges();
  showAdminSuccess(`Challenge "${body.title}" ${operation === 'create' ? 'published' : 'updated'}.`);
}

function openDeleteModal(id, title) {
  deleteTargetId = id;
  document.getElementById('delete-name').textContent = '"' + title + '"';
  document.getElementById('delete-modal').style.display = 'flex';
}
async function confirmDelete() {
  const r = await fetch(`/api/admin/challenges/${encodeURIComponent(deleteTargetId)}`, { method: 'DELETE' });
  document.getElementById('delete-modal').style.display = 'none';
  if (!r.ok) {
    alert('Unable to delete challenge.');
    return;
  }
  await loadAdminChallenges();
  showAdminSuccess('Challenge deleted.');
}
function showAdminSuccess(msg) {
  document.getElementById('admin-success-msg').textContent = '✓ ' + msg;
  document.getElementById('admin-success').style.display = 'flex';
}
document.getElementById('admin-rows').addEventListener('click', (event) => {
  const button = event.target.closest('[data-admin-action]');
  if (!button) return;
  const id = decodeURIComponent(button.dataset.challengeId || '');
  if (button.dataset.adminAction === 'edit') openAdminEdit(id);
  if (button.dataset.adminAction === 'delete') {
    const challenge = adminChallenges.find((item) => item.id === id);
    openDeleteModal(id, challenge?.title || 'Challenge');
  }
});
document.getElementById('close-admin-success').addEventListener('click', () => {
  document.getElementById('admin-success').style.display = 'none';
});
document.getElementById('admin-create-btn').addEventListener('click', openAdminCreate);
document.getElementById('confirm-challenge-delete').addEventListener('click', confirmDelete);
document.getElementById('cancel-challenge-delete').addEventListener('click', () => {
  document.getElementById('delete-modal').style.display = 'none';
});
document.getElementById('form-save-btn').addEventListener('click', saveAdminForm);
document.getElementById('cancel-admin-form').addEventListener('click', closeAdminForm);
loadAdminChallenges();
