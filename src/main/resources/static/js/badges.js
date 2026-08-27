(function () {
    let badges = [];
    let activeFilter = 'all';

    const formatDate = value => value
        ? new Date(value).toLocaleDateString('en-MY', { dateStyle: 'medium' })
        : '';

    const renderStats = () => {
        const earned = badges.filter(badge => badge.earned).length;
        const stats = [
            { label: 'Total', value: badges.length },
            { label: 'Earned', value: earned },
            { label: 'Locked', value: badges.length - earned }
        ];
        document.getElementById('badge-stats').innerHTML = stats.map(stat => `
            <article class="badge-stat-card">
              <span>${stat.label}</span><strong>${stat.value}</strong>
            </article>`).join('');

        document.querySelector('[data-badge-filter="earned"]').textContent = `Earned (${earned})`;
        document.querySelector('[data-badge-filter="locked"]').textContent
            = `Locked (${badges.length - earned})`;
    };

    const renderBadges = () => {
        const grid = document.getElementById('badge-grid');
        const visible = badges.filter(badge => activeFilter === 'all'
            || (activeFilter === 'earned' && badge.earned)
            || (activeFilter === 'locked' && !badge.earned));

        if (!visible.length) {
            grid.innerHTML = '<div class="badge-loading">No badges in this section.</div>';
            return;
        }

        grid.innerHTML = visible.map(badge => {
            const percentage = Math.min(100, Number(badge.progress) / Math.max(1, Number(badge.target)) * 100);
            return `<article class="badge-card ${badge.earned ? 'earned' : 'locked'}">
              ${badge.earned ? '<span class="badge-earned-label">Earned</span>' : ''}
              <span class="badge-emoji" aria-hidden="true">${escapeHtml(badge.emoji || '🏅')}</span>
              <h2>${escapeHtml(badge.name)}</h2>
              <p>${escapeHtml(badge.description)}</p>
              <strong class="badge-criteria">${escapeHtml(badge.unlockCriteria)}</strong>
              ${badge.earned
                ? `<time>${formatDate(badge.dateEarned)}</time>`
                : `<div class="badge-progress"><span style="width:${percentage}%"></span></div>
                   <small>${badge.progress} / ${badge.target} · ${Math.round(percentage)}%</small>`}
            </article>`;
        }).join('');
    };

    const loadBadges = async () => {
        try {
            const response = await fetch('/api/badges');
            if (!response.ok) throw new Error('Unable to load badges');
            badges = await response.json();
            renderStats();
            renderBadges();
        } catch (error) {
            console.error('Unable to load Firebase badges:', error);
            document.getElementById('badge-grid').innerHTML
                = '<div class="badge-loading">Unable to load badges from Firebase.</div>';
        }
    };

    document.querySelectorAll('[data-badge-filter]').forEach(button => {
        button.addEventListener('click', () => {
            activeFilter = button.dataset.badgeFilter;
            document.querySelectorAll('[data-badge-filter]').forEach(tab => {
                tab.classList.toggle('active', tab === button);
            });
            renderBadges();
        });
    });

    loadBadges();
}());
