document.addEventListener('DOMContentLoaded', () => {
    loadHomePoints();
    renderVisits();
});


/* ============================================================
   USER
   ============================================================ */

const HOME_USER_ID =
    localStorage.getItem('warisangoUserId') || 'demo-user';


/* ============================================================
   TIER CONFIGURATION
   ============================================================ */

const HOME_TIERS = [
    {
        name: 'Bronze',
        min: 0,
        max: 499
    },
    {
        name: 'Silver',
        min: 500,
        max: 999
    },
    {
        name: 'Gold',
        min: 1000,
        max: 1999
    },
    {
        name: 'Platinum',
        min: 2000,
        max: Infinity
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

        const response = await fetch(
            `/api/user/points?userId=${encodeURIComponent(HOME_USER_ID)}`
        );


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

    let currentTier =
        HOME_TIERS[0];

    for (const tier of HOME_TIERS) {

        if (points >= tier.min) {
            currentTier = tier;
        }
    }


    /*
     * Find next tier
     */

    const nextTier =
        HOME_TIERS.find(
            tier => tier.min > points
        );


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
            await fetch('/api/visits/recent');


        if (!response.ok) {

            throw new Error(
                `HTTP error! status: ${response.status}`
            );
        }


        const visits =
            await response.json();


        container.innerHTML = '';


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
                document.createElement('div');

            row.className =
                'visit-row';


            const img =
                document.createElement('img');

            img.src =
                v.image || '';

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
                v.date || '';


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


            row.appendChild(img);

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