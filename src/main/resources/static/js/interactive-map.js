document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Leaflet Map
    const map = L.map('map-canvas').setView([3.8000, 101.5000], 7);

    // 2. Load OpenStreetMap Tile Layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    const markersMap = new Map();
    const checkedInToday = new Set();
    const listContainer = document.getElementById('locations-list');
    const locationsCountEl = document.getElementById('locations-count');

    // DOM Filter Elements
    const searchInput = document.getElementById('map-search-input');
    const searchBtn = document.getElementById('map-search-btn');
    const stateSelect = document.getElementById('state-select');
    const citySelect = document.getElementById('city-select');
    const radiusSelect = document.getElementById('radius-select');
    const sortSelect = document.getElementById('sort-select');
    const clearFiltersButton = document.getElementById('map-clear-filters');

    // Master state store
    let rawLocations = [];
    const savedBusinessIds = new Set();

    const bookmarkSvg = `
        <svg viewBox="0 0 24 28" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M4 2h16v23l-8-5-8 5z"></path>
        </svg>`;

    const updateSavedControls = businessId => {
        const saved = savedBusinessIds.has(businessId);
        document.querySelectorAll(`[data-map-save-business="${CSS.escape(businessId)}"]`).forEach(button => {
            button.setAttribute('aria-pressed', String(saved));
            button.setAttribute('aria-label', `${saved ? 'Remove' : 'Save'} this business`);
            button.title = saved ? 'Remove from saved listings' : 'Save business';
        });
    };

    const toggleSavedBusiness = async (businessId, button) => {
        const wasSaved = savedBusinessIds.has(businessId);
        button.disabled = true;
        try {
            const response = await fetch(`/api/saved-listings/${encodeURIComponent(businessId)}`, {
                method: wasSaved ? 'DELETE' : 'POST',
                headers: {'Accept': 'application/json'}
            });
            if (!response.ok) throw new Error('Unable to update saved listing.');
            if (wasSaved) savedBusinessIds.delete(businessId);
            else savedBusinessIds.add(businessId);
            updateSavedControls(businessId);
        } catch (error) {
            console.error(error);
        } finally {
            button.disabled = false;
        }
    };

    // --- User Location State ---
    let userMarker = null;
    let touristLat = null;
    let touristLng = null;
    let isInitialLocationSet = false; 
    let watchId = null;

    const markSidebarCheckedInToday = businessId => {
        const card = Array.from(document.querySelectorAll('.location-item-card'))
            .find(item => item.dataset.businessId === businessId);
        if (!card || card.querySelector('.map-visited-label')) return;
        const label = document.createElement('span');
        label.className = 'map-visited-label';
        label.textContent = '✓ Checked In Today';
        card.querySelector('.location-item-info')?.appendChild(label);
    };

    window.addEventListener('warisango:check-in-complete', (event) => {
        if (event.detail?.businessId) {
            const businessId = event.detail.businessId;
            checkedInToday.add(businessId);

            const marker = markersMap.get(businessId);
            const popupContent = marker?.getPopup()?.getContent();
            if (popupContent instanceof HTMLElement) {
                const checkInButton = popupContent.querySelector('.btn-popup-check-in');
                const checkInStatus = popupContent.querySelector('.popup-check-in-status');
                if (checkInButton) {
                    checkInButton.disabled = true;
                    checkInButton.textContent = 'Checked In Today';
                }
                if (checkInStatus) {
                    const pointsEarned = Number(event.detail.pointsEarned || 0);
                    checkInStatus.textContent = `Checked in successfully. You earned ${pointsEarned} points.`;
                    checkInStatus.classList.add('check-in-success');
                }
            }

            markSidebarCheckedInToday(businessId);
        }
    });

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
        if (!stateSelect) return;

        const selectedState = stateSelect.value;
        stateSelect.replaceChildren(new Option('All States', 'All'));

        const statesInDb = [...new Set(items
            .map(item => item.state?.trim())
            .filter(Boolean))]
            .sort((a, b) => a.localeCompare(b));
        statesInDb.forEach(stateName => {
            stateSelect.appendChild(new Option(stateName, stateName));
        });

        const selectionStillExists = Array.from(stateSelect.options)
            .some(option => option.value === selectedState);
        stateSelect.value = selectionStillExists ? selectedState : 'All';
    };

    const populateCityDropdown = items => {
        if (!citySelect) return;
        const selectedCity = citySelect.value;
        const selectedState = (stateSelect?.value || 'All').trim().toLowerCase();
        const cities = [...new Set(items.filter(item => {
            const state = (item.state || '').trim().toLowerCase();
            return selectedState === 'all' || state === selectedState;
        }).map(item => item.city?.trim()).filter(Boolean))].sort((a, b) => a.localeCompare(b));
        citySelect.replaceChildren(new Option('All Cities', 'All'));
        cities.forEach(city => citySelect.appendChild(new Option(city, city)));
        citySelect.value = cities.includes(selectedCity) ? selectedCity : 'All';
    };

    // ==========================================
    // --- UNIFIED FILTER & SORT PIPELINE ---
    // ==========================================
    const applyFilters = async () => {
        const query = (searchInput?.value || '').toLowerCase().trim();
        const selectedState = (stateSelect?.value || 'All').trim().toLowerCase();
        const selectedCity = (citySelect?.value || 'All').trim().toLowerCase();
        const selectedRadius = parseFloat(radiusSelect?.value || '0');
        const selectedSort = sortSelect?.value || 'none';

        if (clearFiltersButton) {
            const hasActiveFilter = Boolean(query)
                || selectedState !== 'all'
                || selectedCity !== 'all'
                || selectedRadius > 0
                || selectedSort !== 'none';
            clearFiltersButton.style.display = hasActiveFilter ? 'block' : '';
        }

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

            const locCity = (loc.city || '').trim().toLowerCase();
            const matchesCity = selectedCity === 'all' || locCity === selectedCity;

            return matchesSearch && matchesState && matchesCity;
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
        if (selectedSort === 'rating-desc' || selectedSort === 'rating-asc') {
            const direction = selectedSort === 'rating-desc' ? -1 : 1;
            candidates.sort((a, b) => {
                if (a.averageRating == null && b.averageRating == null) return 0;
                if (a.averageRating == null) return 1;
                if (b.averageRating == null) return -1;
                return (a.averageRating - b.averageRating) * direction;
            });
        } else if (selectedSort === 'distance-asc' || selectedSort === 'distance-desc') {
            const direction = selectedSort === 'distance-desc' ? -1 : 1;
            candidates.sort((a, b) => {
                if (a._distanceKm == null && b._distanceKm == null) return 0;
                if (a._distanceKm == null) return 1;
                if (b._distanceKm == null) return -1;
                return (a._distanceKm - b._distanceKm) * direction;
            });
        } else if (selectedSort === 'name-asc' || selectedSort === 'name-desc') {
            const direction = selectedSort === 'name-desc' ? -1 : 1;
            candidates.sort((a, b) => (a.name || '').localeCompare(b.name || '') * direction);
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

            const popupStatus = document.createElement('p');
            popupStatus.className = 'popup-check-in-status';
            popupStatus.setAttribute('aria-live', 'polite');
            popupStatus.textContent = 'Distance verification is disabled during testing.';

            const btnDetails = document.createElement('button');
            btnDetails.className = 'btn-popup-primary';
            btnDetails.textContent = 'View Details';
            btnDetails.addEventListener('click', () => {
                window.location.assign(`/business/${encodeURIComponent(loc.businessId)}`);
            });

            const btnGmaps = document.createElement('a');
            btnGmaps.className = 'btn-popup-outlined';
            const googleMapsQuery = [loc.name, loc.address, loc.city, loc.state]
                .filter(Boolean)
                .join(', ');
            btnGmaps.href = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(googleMapsQuery)}`;
            btnGmaps.target = '_blank';
            btnGmaps.rel = 'noopener noreferrer';
            btnGmaps.textContent = 'Google Maps';

            const btnCheckIn = document.createElement('button');
            btnCheckIn.className = 'btn-popup-check-in';
            btnCheckIn.type = 'button';
            btnCheckIn.textContent = 'Check In';
            btnCheckIn.addEventListener('click', () => {
                window.WarisanGoCheckIn.checkIn(loc, btnCheckIn, popupStatus);
            });

            if (checkedInToday.has(loc.businessId)) {
                btnCheckIn.disabled = true;
                btnCheckIn.textContent = 'Checked In Today';
                popupStatus.textContent = 'You have already checked in at this business today.';
                popupStatus.classList.add('check-in-success');
            } else {
                window.WarisanGoCheckIn.refreshStatus(loc.businessId, btnCheckIn, popupStatus)
                    .then(isCheckedIn => {
                        if (isCheckedIn) {
                            checkedInToday.add(loc.businessId);
                            markSidebarCheckedInToday(loc.businessId);
                        }
                    });
            }

            popupActions.appendChild(btnDetails);
            popupActions.appendChild(btnGmaps);

            popupNode.appendChild(popupTitle);
            popupNode.appendChild(popupMeta);
            popupNode.appendChild(popupActions);
            popupNode.appendChild(btnCheckIn);
            popupNode.appendChild(popupStatus);

            // Bind Marker
            const marker = L.marker([loc.latitude, loc.longitude]).addTo(map);
            marker.bindPopup(popupNode);
            markersMap.set(loc.businessId, marker);

            // Create Sidebar Card
            const card = document.createElement('div');
            card.className = 'location-item-card';
            card.dataset.businessId = loc.businessId;
            card.tabIndex = 0;
            card.setAttribute('role', 'button');
            card.setAttribute('aria-label', `Show ${loc.name} on the map`);

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

            if (checkedInToday.has(loc.businessId)) {
                const visitedLabel = document.createElement('span');
                visitedLabel.className = 'map-visited-label';
                visitedLabel.textContent = '✓ Checked In Today';
                itemInfo.appendChild(visitedLabel);
            }

            const itemRating = document.createElement('div');
            itemRating.className = 'location-item-rating';

            const savedIndicator = document.createElement('button');
            savedIndicator.className = 'map-saved-indicator';
            savedIndicator.type = 'button';
            savedIndicator.dataset.mapSaveBusiness = loc.businessId;
            savedIndicator.innerHTML = bookmarkSvg;
            savedIndicator.setAttribute('aria-pressed', String(savedBusinessIds.has(loc.businessId)));
            savedIndicator.setAttribute('aria-label',
                `${savedBusinessIds.has(loc.businessId) ? 'Remove' : 'Save'} ${loc.name}`);
            savedIndicator.addEventListener('click', event => {
                event.stopPropagation();
                toggleSavedBusiness(loc.businessId, savedIndicator);
            });
            itemRating.textContent = `★ ${ratingDisplay}`;

            const itemActions = document.createElement('div');
            itemActions.className = 'location-item-actions';
            itemActions.appendChild(savedIndicator);
            itemActions.appendChild(itemRating);

            card.appendChild(itemInfo);
            card.appendChild(itemActions);

            const highlightSidebarEntry = () => {
                document.querySelectorAll('.location-item-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
                card.scrollIntoView({block: 'nearest', behavior: 'smooth'});
            };
            const selectLocation = () => {
                highlightSidebarEntry();
                map.flyTo([loc.latitude, loc.longitude], 14, { duration: 1.2 });
                marker.openPopup();
            };
            marker.on('click', highlightSidebarEntry);
            card.addEventListener('click', selectLocation);
            card.addEventListener('keydown', event => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    selectLocation();
                }
            });

            listContainer.appendChild(card);
            updateSavedControls(loc.businessId);
        });

        if (locationsCountEl) {
            locationsCountEl.textContent = `${items.length} locations`;
        }
    };

    // --- Filter Event Listeners ---
    if (radiusSelect) radiusSelect.addEventListener('change', applyFilters);
    if (stateSelect) stateSelect.addEventListener('change', () => {
        populateCityDropdown(rawLocations);
        applyFilters();
    });
    if (citySelect) citySelect.addEventListener('change', applyFilters);
    if (sortSelect) sortSelect.addEventListener('change', applyFilters);
    if (searchBtn) searchBtn.addEventListener('click', applyFilters);
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            if (clearFiltersButton && searchInput.value.trim()) {
                clearFiltersButton.style.display = 'block';
            } else if (clearFiltersButton) {
                const hasOtherFilter = stateSelect?.value !== 'All'
                    || citySelect?.value !== 'All'
                    || radiusSelect?.value !== '0'
                    || sortSelect?.value !== 'none';
                clearFiltersButton.style.display = hasOtherFilter ? 'block' : '';
            }
        });
        searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') applyFilters();
        });
    }
    if (clearFiltersButton) clearFiltersButton.addEventListener('click', () => {
        if (searchInput) searchInput.value = '';
        if (stateSelect) stateSelect.value = 'All';
        populateCityDropdown(rawLocations);
        if (citySelect) citySelect.value = 'All';
        if (radiusSelect) radiusSelect.value = '0';
        if (sortSelect) sortSelect.value = 'none';
        applyFilters();
    });

    // --- SSE Real-Time Stream Integration ---
    const eventSource = new EventSource('/interactive-map/api/stream');

    eventSource.addEventListener('business-update', (event) => {
        rawLocations = JSON.parse(event.data);
        populateStateDropdown(rawLocations);
        populateCityDropdown(rawLocations);
        applyFilters();
    });

    eventSource.onerror = (error) => {
        console.error('SSE connection error / disconnected:', error);
    };

    if (!document.getElementById('guest-gate-modal')) fetch('/api/saved-listings/ids', {headers: {'Accept': 'application/json'}})
        .then(response => response.ok
            ? response.json()
            : Promise.reject(new Error('Unable to load saved listings.')))
        .then(ids => {
            ids.forEach(id => savedBusinessIds.add(id));
            applyFilters();
        })
        .catch(error => console.error(error));

});
