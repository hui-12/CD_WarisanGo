document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Leaflet Map (Centered around Peninsular Malaysia)
    const map = L.map('map-canvas').setView([3.8000, 101.5000], 7);

    // 2. Load OpenStreetMap Tile Layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    const markersMap = new Map();
    const listContainer = document.getElementById('locations-list');
    const locationsCountEl = document.getElementById('locations-count');

    // 3. Render Markers & Sidebar Items dynamically
    const renderLocations = (items) => {
        markersMap.forEach(marker => map.removeLayer(marker));
        markersMap.clear();
        listContainer.innerHTML = '';

        if (!items || items.length === 0) {
            listContainer.innerHTML = '<div class="empty-list-notice">No approved heritage businesses found.</div>';
            if (locationsCountEl) locationsCountEl.textContent = '0 locations';
            return;
        }

        items.forEach(loc => {
            const ratingDisplay = loc.averageRating ? loc.averageRating.toFixed(1) : 'N/A';
            
            const popupContent = `
                <div class="custom-map-popup">
                    <h4 class="popup-title">${escapeHtml(loc.name)}</h4>
                    <p class="popup-meta">${escapeHtml(loc.city || loc.state)} · ★ ${ratingDisplay}</p>
                    <div class="popup-actions">
                        <button class="btn-popup-primary" onclick="alert('Viewing details for ${escapeHtml(loc.name)}')">View Details</button>
                        <a href="https://maps.google.com/?q=${loc.latitude},${loc.longitude}" target="_blank" class="btn-popup-outlined" rel="noopener noreferrer">Google Maps</a>
                    </div>
                </div>
            `;

            const marker = L.marker([loc.latitude, loc.longitude]).addTo(map);
            marker.bindPopup(popupContent);
            markersMap.set(loc.businessId, marker);

            const card = document.createElement('button');
            card.className = 'location-item-card';
            card.innerHTML = `
                <div class="location-item-info">
                    <div class="location-item-name">${escapeHtml(loc.name)}</div>
                    <div class="location-item-meta">${escapeHtml(loc.city || '')} · ${escapeHtml(loc.description || loc.address || '')}</div>
                </div>
                <div class="location-item-rating">★ ${ratingDisplay}</div>
            `;

            card.addEventListener('click', () => {
                document.querySelectorAll('.location-item-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
                
                map.flyTo([loc.latitude, loc.longitude], 14, { duration: 1.2 });
                marker.openPopup();
            });

            listContainer.appendChild(card);
        });

        if (locationsCountEl) {
            locationsCountEl.textContent = `${items.length} locations`;
        }
    };

    // 4. Connect to SSE Stream for Real-Time Database Updates
    const eventSource = new EventSource('/interactive-map/api/stream');

    eventSource.addEventListener('business-update', (event) => {
        const data = JSON.parse(event.data);
        renderLocations(data);
    });

    eventSource.onerror = (error) => {
        console.error('SSE connection error / disconnected:', error);
    };
});