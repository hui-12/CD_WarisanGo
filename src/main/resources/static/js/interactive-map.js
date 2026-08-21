document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Leaflet Map
    const map = L.map('map-canvas').setView([3.8000, 101.5000], 7);

    // 2. Load OpenStreetMap Tile Layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    const markersMap = new Map();
    const listContainer = document.getElementById('locations-list');
    const locationsCountEl = document.getElementById('locations-count');

    // DOM Filter Elements
    const searchInput = document.getElementById('map-search-input');
    const searchBtn = document.getElementById('map-search-btn');
    const stateSelect = document.getElementById('state-select');
    const radiusSelect = document.getElementById('radius-select');
    const sortSelect = document.getElementById('sort-select');

    // Master state store
    let rawLocations = [];

    // --- User Location State ---
    let userMarker = null;
    let touristLat = null;
    let touristLng = null;
    let isInitialLocationSet = false; 
    let watchId = null;

    // --- Custom Icon for Tourist Location ---
    const userLocationIcon = L.divIcon({
        className: 'user-location-marker',
        html: '<div class="user-dot"></div>',
        iconSize: [24, 24],
        iconAnchor: [12, 12]
    });

    // --- Geolocation Handler ---
    const locateTourist = () => {
        if ("geolocation" in navigator) {
            if (watchId !== null) {
                navigator.geolocation.clearWatch(watchId);
            }

            watchId = navigator.geolocation.watchPosition(
                (position) => {
                    touristLat = position.coords.latitude;
                    touristLng = position.coords.longitude;

                    if (userMarker) {
                        userMarker.setLatLng([touristLat, touristLng]);
                    } else {
                        userMarker = L.marker([touristLat, touristLng], { icon: userLocationIcon }).addTo(map);
                        userMarker.bindPopup(`
                            <div class="custom-map-popup">
                                <h4 class="popup-title">Your Location</h4>
                                <p class="popup-meta">Current detected coordinates</p>
                            </div>
                        `);
                    }

                    if (!isInitialLocationSet) {
                        map.flyTo([touristLat, touristLng], 14, { duration: 1.2 });
                        isInitialLocationSet = true;
                    }

                    applyFilters();
                },
                (error) => {
                    console.warn('Geolocation error:', error.message);
                    
                    if (userMarker) {
                        map.removeLayer(userMarker);
                        userMarker = null;
                    }
                    
                    touristLat = null;
                    touristLng = null;
                    isInitialLocationSet = false; 
                    
                    applyFilters();
                },
                { enableHighAccuracy: true, maximumAge: 0 }
            );
        }
    };

    locateTourist();

    // Permissions & Focus Event Listeners
    if ("permissions" in navigator) {
        navigator.permissions.query({ name: 'geolocation' }).then((permissionStatus) => {
            permissionStatus.onchange = () => {
                if (permissionStatus.state === 'granted' || permissionStatus.state === 'prompt') {
                    locateTourist();
                } else if (permissionStatus.state === 'denied') {
                    if (userMarker) {
                        map.removeLayer(userMarker);
                        userMarker = null;
                    }
                    touristLat = null;
                    touristLng = null;
                    isInitialLocationSet = false;
                    applyFilters();
                }
            };
        }).catch((err) => console.warn('Permissions API error:', err));
    }

    window.addEventListener('focus', () => {
        if (!touristLat || !touristLng) {
            locateTourist();
        }
    });

    // --- Custom Leaflet "Locate Me" Button ---
    const locateControl = L.control({ position: 'bottomright' });
    
    locateControl.onAdd = function() {
        const btn = L.DomUtil.create('button', 'btn-locate-me');
        btn.innerHTML = '📍';
        btn.title = 'Focus on my location';
        
        L.DomEvent.disableClickPropagation(btn);
        
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            if (touristLat && touristLng) {
                map.flyTo([touristLat, touristLng], 14, { duration: 1.2 });
            } else {
                locateTourist();
            }
        });

        return btn;
    };
    
    locateControl.addTo(map);

    // =======================================================
    // --- SINGLE OSRM PATH DISTANCE CALCULATION ---
    // =======================================================
    const fetchPathDistances = async (touristLat, touristLng, locations) => {
        if (!locations.length || touristLat === null || touristLng === null) return locations;

        const targetLocations = locations.slice(0, 20);

        const coords = [
            `${touristLng},${touristLat}`,
            ...targetLocations.map(loc => `${loc.longitude},${loc.latitude}`)
        ].join(';');

        const drivingUrl = `https://router.project-osrm.org/table/v1/driving/${coords}?sources=0&annotations=distance`;

        let driveMeters = [];
        try {
            const driveRes = await fetch(drivingUrl);
            if (driveRes.ok) {
                const data = await driveRes.json();
                driveMeters = data?.distances?.[0] || [];
            }
        } catch (e) {
            console.warn('Driving route calculation failed, using straight-line fallback:', e);
        }

        return locations.map((loc, idx) => {
            let distKm;
            if (idx < 20 && driveMeters[idx + 1] != null) {
                distKm = driveMeters[idx + 1] / 1000;
            } else {
                const touristLatLng = L.latLng(touristLat, touristLng);
                const businessLatLng = L.latLng(loc.latitude, loc.longitude);
                distKm = touristLatLng.distanceTo(businessLatLng) / 1000;
            }

            return {
                ...loc,
                _distanceKm: distKm
            };
        });
    };

    // Helper: Dynamic populate states from DB items
    const populateStateDropdown = (items) => {
        if (!stateSelect || stateSelect.options.length > 4) return;

        const statesInDb = [...new Set(items.map(i => i.state).filter(Boolean))].sort();
        statesInDb.forEach(stateName => {
            const exists = Array.from(stateSelect.options).some(opt => opt.value.toLowerCase() === stateName.toLowerCase());
            if (!exists) {
                const opt = document.createElement('option');
                opt.value = stateName;
                opt.textContent = stateName;
                stateSelect.appendChild(opt);
            }
        });
    };

    // ==========================================
    // --- UNIFIED FILTER & SORT PIPELINE ---
    // ==========================================
    const applyFilters = async () => {
        const query = (searchInput?.value || '').toLowerCase().trim();
        const selectedState = (stateSelect?.value || 'All').trim().toLowerCase();
        const selectedRadius = parseFloat(radiusSelect?.value || '0');
        const selectedSort = sortSelect?.value || 'none';

        let locationMissingNotice = false;

        // 1. Phase 1: Local Search & Case-Insensitive State Filter
        let candidates = rawLocations.filter(loc => {
            const matchesSearch = !query || 
                (loc.name && loc.name.toLowerCase().includes(query)) ||
                (loc.city && loc.city.toLowerCase().includes(query));

            const locState = (loc.state || '').trim().toLowerCase();
            const matchesState = (selectedState === 'all') || 
                (locState === selectedState) ||
                (locState.includes(selectedState)) ||
                (selectedState.includes(locState));

            return matchesSearch && matchesState;
        });

        // 2. Phase 2: Calculate Single Distance Whenever Location Is Available
        if (touristLat !== null && touristLng !== null) {
            candidates = await fetchPathDistances(touristLat, touristLng, candidates);

            // Filter by radius if a specific distance is selected
            if (selectedRadius > 0) {
                candidates = candidates.filter(loc => loc._distanceKm !== undefined && loc._distanceKm <= selectedRadius);
            }
        } else if (selectedRadius > 0) {
            locationMissingNotice = true;
            candidates = [];
        }

        // 3. Phase 3: Sorting
        if (selectedSort === 'rating-desc') {
            candidates.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
        }

        renderLocations(candidates, locationMissingNotice);
    };

    // --- Render Markers & Sidebar Items ---
    const renderLocations = (items, isLocationBlockedForRadius = false) => {
        markersMap.forEach(marker => map.removeLayer(marker));
        markersMap.clear();
        listContainer.innerHTML = '';

        if (!items || items.length === 0) {
            const emptyNotice = document.createElement('div');
            emptyNotice.className = 'empty-list-notice';
            emptyNotice.style.padding = '16px';
            emptyNotice.style.fontSize = '12px';
            emptyNotice.style.color = '#6b6b68';
            emptyNotice.style.textAlign = 'center';

            if (isLocationBlockedForRadius) {
                emptyNotice.textContent = '📍 Please enable location services to filter businesses within your chosen radius.';
            } else {
                emptyNotice.textContent = 'No approved heritage businesses found matching your criteria.';
            }

            listContainer.appendChild(emptyNotice);

            if (locationsCountEl) locationsCountEl.textContent = '0 locations';
            return;
        }

        items.forEach(loc => {
            const ratingDisplay = loc.averageRating ? loc.averageRating.toFixed(1) : 'N/A';
            const distanceText = loc._distanceKm !== undefined ? ` · ${loc._distanceKm.toFixed(1)} km away` : '';

            // Create Leaflet Popup DOM
            const popupNode = document.createElement('div');
            popupNode.className = 'custom-map-popup';

            const popupTitle = document.createElement('h4');
            popupTitle.className = 'popup-title';
            popupTitle.textContent = loc.name;

            const popupMeta = document.createElement('p');
            popupMeta.className = 'popup-meta';
            popupMeta.textContent = `${loc.city || loc.state} · ★ ${ratingDisplay}${distanceText}`;

            const popupActions = document.createElement('div');
            popupActions.className = 'popup-actions';

            const btnDetails = document.createElement('button');
            btnDetails.className = 'btn-popup-primary';
            btnDetails.textContent = 'View Details';
            btnDetails.addEventListener('click', () => {
                alert(`Viewing details for ${loc.name}`);
            });

            const btnGmaps = document.createElement('a');
            btnGmaps.className = 'btn-popup-outlined';
            btnGmaps.href = `https://maps.google.com/?q=${loc.latitude},${loc.longitude}`;
            btnGmaps.target = '_blank';
            btnGmaps.rel = 'noopener noreferrer';
            btnGmaps.textContent = 'Google Maps';

            popupActions.appendChild(btnDetails);
            popupActions.appendChild(btnGmaps);

            popupNode.appendChild(popupTitle);
            popupNode.appendChild(popupMeta);
            popupNode.appendChild(popupActions);

            // Bind Marker
            const marker = L.marker([loc.latitude, loc.longitude]).addTo(map);
            marker.bindPopup(popupNode);
            markersMap.set(loc.businessId, marker);

            // Create Sidebar Card
            const card = document.createElement('button');
            card.className = 'location-item-card';

            const itemInfo = document.createElement('div');
            itemInfo.className = 'location-item-info';

            const itemName = document.createElement('div');
            itemName.className = 'location-item-name';
            itemName.textContent = loc.name;

            const itemMeta = document.createElement('div');
            itemMeta.className = 'location-item-meta';
            itemMeta.textContent = `${loc.city || loc.state || ''}${distanceText}`;

            itemInfo.appendChild(itemName);
            itemInfo.appendChild(itemMeta);

            const itemRating = document.createElement('div');
            itemRating.className = 'location-item-rating';
            itemRating.textContent = `★ ${ratingDisplay}`;

            card.appendChild(itemInfo);
            card.appendChild(itemRating);

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

    // --- Filter Event Listeners ---
    if (radiusSelect) radiusSelect.addEventListener('change', applyFilters);
    if (stateSelect) stateSelect.addEventListener('change', applyFilters);
    if (sortSelect) sortSelect.addEventListener('change', applyFilters);
    if (searchBtn) searchBtn.addEventListener('click', applyFilters);
    if (searchInput) {
        searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') applyFilters();
        });
    }

    // --- SSE Real-Time Stream Integration ---
    const eventSource = new EventSource('/interactive-map/api/stream');

    eventSource.addEventListener('business-update', (event) => {
        rawLocations = JSON.parse(event.data);
        populateStateDropdown(rawLocations);
        applyFilters();
    });

    eventSource.onerror = (error) => {
        console.error('SSE connection error / disconnected:', error);
    };
});