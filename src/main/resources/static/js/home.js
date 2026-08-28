document.addEventListener('DOMContentLoaded', () => {
    loadHomePoints();
    renderVisits();
    renderActiveChallenges();
    loadQuickLinkStats();
});


/* ============================================================
   TIER CONFIGURATION
   ============================================================ */

const HOME_TIERS = [
    {
        name: 'Bronze',
        min: 0,
        max: 499,
        color: '#cd7f32'
    },
    {
        name: 'Silver',
        min: 500,
        max: 999,
        color: '#a8a8a8'
    },
    {
        name: 'Gold',
        min: 1000,
        max: 1999,
        color: '#c9a84c'
    },
    {
        name: 'Platinum',
        min: 2000,
        max: Infinity,
        color: '#6ab0c8'
    }
];


/* ============================================================
   LOAD POINTS FROM FIREBASE
   ============================================================ */

async function loadHomePoints() {

    const tierNameEl =
        document.getElementById('tier-name');

    const pointsEl =
        document.getElementById('user-points');

    const progressEl =
        document.getElementById('tier-progress-bar');

    const nextInfoEl =
        document.getElementById('tier-next-info');


    if (!tierNameEl ||
        !pointsEl ||
        !progressEl ||
        !nextInfoEl) {
        return;
    }


    try {

        const response = await fetch('/api/user/points');


        if (!response.ok) {
            throw new Error(
                `Unable to load points. HTTP ${response.status}`
            );
        }


        const data = await response.json();

        const points =
            Number(data.currentPoints || 0);


        renderHomeTier(points);


    } catch (error) {

        console.error(
            'Failed to load Firebase points:',
            error
        );


        tierNameEl.textContent = 'Unavailable';

        pointsEl.textContent = '—';

        progressEl.style.width = '0%';

        nextInfoEl.textContent =
            'Unable to load points';
    }
}


/* ============================================================
   RENDER TIER
   ============================================================ */

function renderHomeTier(points) {

    const tierNameEl =
        document.getElementById('tier-name');

    const pointsEl =
        document.getElementById('user-points');

    const progressEl =
        document.getElementById('tier-progress-bar');

    const nextInfoEl =
        document.getElementById('tier-next-info');


    if (!tierNameEl ||
        !pointsEl ||
        !progressEl ||
        !nextInfoEl) {
        return;
    }


    /*
     * Find the user's current tier
     */

    let currentTier = HOME_TIERS[0];
    for (const tier of HOME_TIERS) {
        if (points >= tier.min) currentTier = tier;
    }


    /*
     * Find next tier
     */

    const currentTierIndex = HOME_TIERS.indexOf(currentTier);
    const nextTier = HOME_TIERS[currentTierIndex + 1];


    /*
     * Display current points
     */

    pointsEl.textContent =
        points.toLocaleString();


    /*
     * Display current tier
     */

    tierNameEl.textContent =
        currentTier.name;
    tierNameEl.style.color = currentTier.color;
    progressEl.style.backgroundColor = currentTier.color;


    /*
     * Calculate progress
     */

    if (nextTier) {

        const pointsIntoTier =
            points - currentTier.min;

        const tierRange =
            nextTier.min - currentTier.min;

        const progress =
            (pointsIntoTier / tierRange) * 100;


        progressEl.style.width =
            `${Math.max(
                0,
                Math.min(100, progress)
            )}%`;


        const remaining =
            nextTier.min - points;


        nextInfoEl.textContent =
            `${remaining.toLocaleString()} pts to ${nextTier.name}`;

    } else {

        /*
         * Platinum is the highest tier
         */

        progressEl.style.width = '100%';

        nextInfoEl.textContent =
            'Highest tier reached';
    }
}


/* ============================================================
   RECENT VISITS
   ============================================================ */

const renderVisits = async () => {

    const container =
        document.getElementById(
            'visits-list-container'
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch('/api/visits/this-week');


        if (!response.ok) {

            throw new Error(
                `HTTP error! status: ${response.status}`
            );
        }


        const visits =
            await response.json();


        container.innerHTML = '';

        const count = document.getElementById('visit-count');
        if (count) count.textContent = `${visits.length} check-in${visits.length === 1 ? '' : 's'}`;


        if (!Array.isArray(visits) ||
            visits.length === 0) {

            const emptyEl =
                document.createElement('div');

            emptyEl.className =
                'empty-list-notice';

            emptyEl.textContent =
                'No recent visits found.';

            container.appendChild(
                emptyEl
            );

            return;
        }


        visits.forEach(v => {

            const row =
                document.createElement('a');

            row.className =
                'visit-row';
            row.href = `/business/${encodeURIComponent(v.businessId)}`;
            row.setAttribute('aria-label', `View details for ${v.businessName || 'heritage business'}`);


            const img =
                document.createElement('img');

            img.src =
                v.image || '/images/business-default.jpg';

            img.alt =
                v.businessName ||
                'Business Visit';

            img.className =
                'visit-img';


            const infoDiv =
                document.createElement('div');


            const nameDiv =
                document.createElement('div');

            nameDiv.className =
                'visit-name';

            nameDiv.textContent =
                v.businessName ||
                'Unknown Business';


            const dateDiv =
                document.createElement('div');

            dateDiv.className =
                'visit-date';

            dateDiv.textContent =
                v.visitedAt ? new Date(v.visitedAt).toLocaleString('en-MY', {
                    dateStyle: 'medium', timeStyle: 'short'
                }) : '';


            infoDiv.appendChild(
                nameDiv
            );

            infoDiv.appendChild(
                dateDiv
            );


            const pointsDiv =
                document.createElement('div');

            pointsDiv.className =
                'points-tag';

            pointsDiv.textContent =
                `+${v.points ?? 0}`;


            if (v.image) row.appendChild(img);

            row.appendChild(infoDiv);

            row.appendChild(pointsDiv);


            container.appendChild(row);
        });


    } catch (error) {

        console.error(
            'Error fetching database visits:',
            error
        );
    }
};

const renderActiveChallenges = async () => {
    const widget = document.getElementById('active-challenge-widget');
    if (!widget) return;

    try {
        const response = await fetch('/api/challenges');
        if (!response.ok) throw new Error('Unable to load challenges');
        const challenges = await response.json();
        const active = challenges.filter(challenge => challenge.joined && !challenge.done);

        if (!active.length) {
            widget.innerHTML = '<p class="widget-label">ACTIVE CHALLENGES</p>'
                + '<p class="challenge-empty">No challenges in progress.</p>'
                + '<button class="btn-outlined-full" data-navigate="challenges">Browse Challenges</button>';
            return;
        }

        widget.innerHTML = '<p class="widget-label">ACTIVE CHALLENGES</p>'
            + '<div class="home-challenge-list">'
            + active.map(challenge => {
                const target = Math.max(1, Number(challenge.target || 1));
                const progress = Math.min(target, Number(challenge.progress || 0));
                const percentage = Math.min(100, progress / target * 100);
                return `<article class="home-challenge-item">
                    <h3 class="challenge-title">${escapeHtml(challenge.title || 'Challenge')}</h3>
                    <div class="challenge-progress-header"><span class="sub-label">Progress</span>
                    <span class="challenge-count">${progress}/${target}</span></div>
                    <div class="progress-bar-bg-dark"><div class="progress-bar-fill gold-bg"
                    style="width:${percentage}%"></div></div>
                </article>`;
            }).join('') + '</div>'
            + '<button class="btn-outlined-full" data-navigate="challenges">View All Challenges →</button>';
    } catch (error) {
        console.error('Unable to load active challenges:', error);
        widget.innerHTML = '<p class="widget-label">ACTIVE CHALLENGES</p>'
            + '<p class="challenge-empty">Unable to load challenges.</p>';
    }
};

const loadQuickLinkStats = async () => {
    const badgeElement = document.getElementById('home-badges-earned');
    const rankElement = document.getElementById('home-leaderboard-rank');

    try {
        const response = await fetch('/api/badges/summary');
        if (!response.ok) throw new Error('Unable to load badge summary');
        const summary = await response.json();
        if (badgeElement) badgeElement.textContent = `${summary.earned} of ${summary.total} earned`;
    } catch (error) {
        console.warn('Unable to load home badge summary:', error);
        if (badgeElement) badgeElement.textContent = 'Badges unavailable';
    }

    try {
        const response = await fetch('/api/points/leaderboard/me');
        if (!response.ok) throw new Error('Unable to load leaderboard rank');
        const entry = await response.json();
        if (rankElement) rankElement.textContent = `You are #${entry.rank}`;
    } catch (error) {
        console.warn('Unable to load home leaderboard rank:', error);
        if (rankElement) rankElement.textContent = 'Rank unavailable';
    }
};
