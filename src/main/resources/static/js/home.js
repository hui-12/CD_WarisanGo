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
        const visits = await response.json();

        container.innerHTML = '';
        visits.forEach(v => {
            const row = document.createElement('div');
            row.className = 'visit-row';
            row.innerHTML = `
                <img src="${v.image}" alt="${escapeHtml(v.businessName)}" class="visit-img">
                <div>
                    <div class="visit-name">${escapeHtml(v.businessName)}</div>
                    <div class="visit-date">${escapeHtml(v.date)}</div>
                </div>
                <div class="points-tag">+${v.points}</div>
            `;
            container.appendChild(row);
        });
    } catch (error) {
        console.error('Error fetching database visits:', error);
    }
};