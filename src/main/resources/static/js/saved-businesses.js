document.addEventListener('DOMContentLoaded', () => {
  const results = document.getElementById('saved-results');
  const summary = document.getElementById('saved-summary');
  const filters = document.getElementById('saved-filters');
  const searchInput = document.getElementById('saved-search');
  const searchButton = document.getElementById('saved-search-button');
  const stateFilter = document.getElementById('saved-state');
  const cityFilter = document.getElementById('saved-city');
  const clearButton = document.getElementById('saved-clear');
  const resultCount = document.getElementById('saved-result-count');
  let businesses = [];

  const bookmarkSvg = `<svg viewBox="0 0 24 28" fill="none" stroke="currentColor" stroke-width="2"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 2h16v23l-8-5-8 5z"></path></svg>`;
  const escapeHtml = (value) =>
    String(value ?? '').replace(
      /[&<>'"]/g,
      (character) =>
        ({
          '&': '&amp;',
          '<': '&lt;',
          '>': '&gt;',
          "'": '&#39;',
          '"': '&quot;',
        })[character]
    );
  const normalize = (value) =>
    String(value || '')
      .trim()
      .toLowerCase();

  function fillSelect(select, values, label, selectedValue = '') {
    select.replaceChildren(new Option(label, ''));
    [...new Set(values.filter(Boolean))]
      .sort((a, b) => a.localeCompare(b))
      .forEach((value) => select.add(new Option(value, value)));
    if ([...select.options].some((option) => option.value === selectedValue)) select.value = selectedValue;
  }

  function refreshCityOptions() {
    const selectedCity = cityFilter.value;
    const state = stateFilter.value;
    const stateMatches = businesses.filter((item) => !state || item.state === state);
    fillSelect(
      cityFilter,
      stateMatches.map((item) => item.city),
      'All Cities',
      selectedCity
    );
  }

  function render() {
    const query = normalize(searchInput.value);
    const state = stateFilter.value;
    const city = cityFilter.value;
    const displayed = businesses.filter((item) => {
      const searchText = normalize([item.name, item.description, item.address, item.city, item.state].join(' '));
      return (
        (!query || searchText.includes(query)) && (!state || item.state === state) && (!city || item.city === city)
      );
    });
    clearButton.style.display = query || state || city ? 'block' : '';
    resultCount.textContent = businesses.length
      ? `${displayed.length} of ${businesses.length} saved ${displayed.length === 1 ? 'business' : 'businesses'}`
      : '';

    if (!businesses.length) {
      results.innerHTML = `<div class="saved-empty"><span class="saved-empty-icon" aria-hidden="true">◇</span>
        <h2>No saved listings yet</h2><p>Bookmark heritage businesses from the Directory or their details pages.</p>
        <a href="/directory">Explore the directory</a></div>`;
      return;
    }
    if (!displayed.length) {
      results.innerHTML = '<p class="saved-empty">No saved businesses match your search and filters.</p>';
      return;
    }

    results.innerHTML = displayed
      .map((item) => {
        const image = item.imageUrls?.[0];
        const imageContent = image
          ? `<a href="/business/${encodeURIComponent(item.businessId)}"><img class="business-image"
            src="${escapeHtml(image)}" alt="${escapeHtml(item.name)} photo" loading="lazy"></a>`
          : '<p class="business-image-empty">No photos yet</p>';
        const rating = item.averageRating == null ? 'Not rated' : Number(item.averageRating).toFixed(1);
        const ratingClass = item.averageRating == null ? 'rating unrated' : 'rating';
        return `<article class="business-card" data-business-id="${escapeHtml(item.businessId)}">
        <div class="business-image-wrap">
          <button class="save-btn indicator saved-remove" type="button"
                  aria-label="Remove ${escapeHtml(item.name)} from saved listings" aria-pressed="true">
            <span class="bookmark-icon" aria-hidden="true">${bookmarkSvg}</span>
          </button>
          ${imageContent}
        </div>
        <div class="business-content">
          <div class="business-heading"><div><p class="business-kicker">Heritage business · Approved</p>
            <h2>${escapeHtml(item.name)}</h2></div><span class="${ratingClass}">${escapeHtml(rating)}</span></div>
          <p class="business-description">${escapeHtml(item.description || 'No description available.')}</p>
          <div class="business-footer"><span class="business-location">${escapeHtml([item.city, item.state].filter(Boolean).join(', '))}</span>
            <a class="details-link" href="/business/${encodeURIComponent(item.businessId)}">View details <span aria-hidden="true">→</span></a></div>
        </div></article>`;
      })
      .join('');
  }

  async function removeSavedListing(businessId) {
    const response = await fetch(`/api/saved-listings/${encodeURIComponent(businessId)}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('Unable to remove this saved listing.');
    businesses = businesses.filter((item) => item.businessId !== businessId);
    summary.textContent = businesses.length
      ? `${businesses.length} saved heritage ${businesses.length === 1 ? 'business' : 'businesses'}`
      : "You haven't saved any heritage businesses yet.";
    filters.hidden = businesses.length === 0;
    fillSelect(
      stateFilter,
      businesses.map((item) => item.state),
      'All States',
      stateFilter.value
    );
    refreshCityOptions();
    render();
  }

  results.addEventListener('click', (event) => {
    const button = event.target.closest('.saved-remove');
    if (!button) return;
    const card = button.closest('[data-business-id]');
    button.disabled = true;
    removeSavedListing(card.dataset.businessId).catch((error) => {
      button.disabled = false;
      window.alert(error.message);
    });
  });
  searchInput.addEventListener('input', render);
  searchButton.addEventListener('click', render);
  stateFilter.addEventListener('change', () => {
    refreshCityOptions();
    render();
  });
  cityFilter.addEventListener('change', render);
  clearButton.addEventListener('click', () => {
    searchInput.value = '';
    stateFilter.value = '';
    refreshCityOptions();
    cityFilter.value = '';
    render();
  });

  fetch('/api/saved-listings')
    .then((response) => {
      if (!response.ok) throw new Error('Unable to load saved listings.');
      return response.json();
    })
    .then((data) => {
      businesses = data;
      summary.textContent = businesses.length
        ? `${businesses.length} saved heritage ${businesses.length === 1 ? 'business' : 'businesses'}`
        : "You haven't saved any heritage businesses yet.";
      filters.hidden = businesses.length === 0;
      fillSelect(
        stateFilter,
        businesses.map((item) => item.state),
        'All States'
      );
      refreshCityOptions();
      render();
    })
    .catch(() => {
      results.innerHTML = '<p class="saved-empty">Unable to load saved listings from Firebase.</p>';
      summary.textContent = 'Your saved listings are temporarily unavailable.';
    });
});
