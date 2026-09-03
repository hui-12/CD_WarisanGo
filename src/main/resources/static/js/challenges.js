let challenges = [];

async function loadChallenges() {
  const container = document.getElementById('challenges-list');
  try {
    const response = await fetch('/api/challenges');
    if (!response.ok) throw new Error('Unable to load challenges');
    challenges = await response.json();
    renderChallenges();
  } catch (error) {
    console.error(error);
    container.innerHTML = '<div class="card">Unable to load challenges from Firebase.</div>';
  }
}

function renderChallenges() {
  const container = document.getElementById('challenges-list');
  if (!challenges.length) {
    container.innerHTML =
      '<div class="card"><strong>No active challenges.</strong><p class="view-challenge-js-1">Admin can publish a challenge from Admin Challenge.</p></div>';
    return;
  }
  const renderCards = (challengeList) =>
    challengeList
      .map((ch) => {
        const total = Number(ch.target || 1);
        const progress = Math.min(Number(ch.progress || 0), total);
        const pct = Math.min(100, (progress / total) * 100);
        const joined = !!ch.joined;
        const done = !!ch.done;
        const reward = Number(ch.rewardPoints || 0);
        const encodedChallengeId = encodeURIComponent(ch.id).replace(/'/g, '%27');
        return `<div class="card view-challenge-js-2">
      <div>
        <div class="view-challenge-js-3">
          <span class="view-challenge-js-4">${escapeHtml(ch.title || 'Challenge')}</span>
          ${done ? '<span class="view-challenge-js-5">COMPLETED</span>' : ''}
        </div>
        <p class="view-challenge-js-6">${escapeHtml(ch.description || '')}</p>
        <div class="view-challenge-js-7">
          <strong class="view-challenge-js-8">Requirements:</strong> ${escapeHtml(ch.requirement || '')}
        </div>
        ${joined ? `<div class="view-challenge-js-9"><div style="height:100%;width:${pct}%;background:#c9a84c;border-radius:4px"></div></div><div class="view-challenge-js-10">${progress} / ${total} requirements met</div>` : '<div class="view-challenge-js-11">Join this challenge to start tracking progress.</div>'}
      </div>
      <div class="view-challenge-js-12">
        <div class="view-challenge-js-13">+${reward} pts</div>
        <div class="view-challenge-js-14">${escapeHtml(ch.badge || '')}</div>
        <div class="view-challenge-js-15">Expires ${escapeHtml(ch.expiry || '—')}</div>
        ${done ? '<button class="btn-secondary" disabled>Completed</button>' : !joined ? `<button class="btn-primary" data-challenge-action="join" data-challenge-id="${encodedChallengeId}">Join Challenge</button>` : progress >= total ? `<button class="btn-primary" data-challenge-action="claim" data-challenge-id="${encodedChallengeId}">Claim Reward</button>` : '<button class="btn-secondary" disabled>In Progress</button>'}
      </div>
    </div>`;
      })
      .join('');

  const available = challenges.filter((challenge) => !challenge.joined && !challenge.done);
  const inProgress = challenges.filter((challenge) => challenge.joined && !challenge.done);
  const completed = challenges.filter((challenge) => challenge.done);
  const group = (title, items, emptyMessage) => `<section class="challenge-group">
    <div class="challenge-group-heading"><h2>${title}</h2><span>${items.length}</span></div>
    <div class="challenge-group-list">${
      items.length ? renderCards(items) : `<div class="card challenge-group-empty">${emptyMessage}</div>`
    }</div>
  </section>`;

  container.innerHTML =
    group('Active', available, 'No challenges are currently available to join.') +
    group('In Progress', inProgress, 'You have no challenges in progress.') +
    group('Completed', completed, 'You have not completed a challenge yet.');
}

async function joinChallenge(id) {
  const response = await fetch(`/api/challenges/${encodeURIComponent(id)}/join`, { method: 'POST' });
  if (!response.ok) {
    alert('Unable to join challenge.');
    return;
  }
  await loadChallenges();
}

async function claimChallenge(id) {
  const response = await fetch(`/api/challenges/${encodeURIComponent(id)}/claim`, { method: 'POST' });
  const result = await response.json();
  if (!response.ok || !result.success) {
    alert(result.message || 'Unable to claim challenge.');
    return;
  }
  document.getElementById('done-title').textContent = challenges.find((c) => c.id === id)?.title || 'Challenge';
  document.getElementById('done-pts').textContent = '+' + Number(result.pointsEarned || 0);
  document.getElementById('done-badge').textContent = '🏅';
  document.getElementById('complete-modal').style.display = 'flex';
  await loadChallenges();
}

document.getElementById('challenges-list').addEventListener('click', (event) => {
  const button = event.target.closest('[data-challenge-action]');
  if (!button) return;

  const challengeId = decodeURIComponent(button.dataset.challengeId || '');
  if (button.dataset.challengeAction === 'join') {
    joinChallenge(challengeId);
  } else if (button.dataset.challengeAction === 'claim') {
    claimChallenge(challengeId);
  }
});

document.getElementById('close-complete-modal').addEventListener('click', () => {
  document.getElementById('complete-modal').style.display = 'none';
});

loadChallenges();
