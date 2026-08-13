document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('visits-list-container');
    if (container) {
        renderVisits();
    }
});

const renderVisits = async () => {
    const container = document.getElementById('visits-list-container');
    if (!container) return;
    
    try {
        const response = await fetch('/api/visits/recent');
        
        // Validate HTTP response state
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const visits = await response.json();
        container.innerHTML = '';

        if (!Array.isArray(visits) || visits.length === 0) {
            const emptyEl = document.createElement('div');
            emptyEl.className = 'empty-list-notice';
            emptyEl.textContent = 'No recent visits found.';
            container.appendChild(emptyEl);
            return;
        }

        visits.forEach(v => {
            const row = document.createElement('div');
            row.className = 'visit-row';

            const img = document.createElement('img');
            img.src = v.image || ''; // Safe property assignment (No XSS injection)
            img.alt = v.businessName || 'Business Visit';
            img.className = 'visit-img';

            const infoDiv = document.createElement('div');

            const nameDiv = document.createElement('div');
            nameDiv.className = 'visit-name';
            nameDiv.textContent = v.businessName || 'Unknown Business';

            const dateDiv = document.createElement('div');
            dateDiv.className = 'visit-date';
            dateDiv.textContent = v.date || '';

            infoDiv.appendChild(nameDiv);
            infoDiv.appendChild(dateDiv);

            const pointsDiv = document.createElement('div');
            pointsDiv.className = 'points-tag';
            pointsDiv.textContent = `+${v.points ?? 0}`;

            row.appendChild(img);
            row.appendChild(infoDiv);
            row.appendChild(pointsDiv);

            container.appendChild(row);
        });
    } catch (error) {
        console.error('Error fetching database visits:', error);
    }
};