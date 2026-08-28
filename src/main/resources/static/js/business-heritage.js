// Business Heritage JS — handles search, filters and accessibility tweaks
(function(){
  const searchInput = document.getElementById('searchInput');
  const searchButton = document.getElementById('searchButton');
  const stateFilter = document.getElementById('stateFilter');
  const cityFilter = document.getElementById('cityFilter');
  const clearBtn = document.getElementById('clearFilters');
  const resultsContainer = document.getElementById('results');

  function sanitizeText(s){ return (s||'').toString().toLowerCase(); }

  function applyFilters(){
    const q = sanitizeText(searchInput?.value || '');
    const st = sanitizeText(stateFilter?.value || '');
    const ct = sanitizeText(cityFilter?.value || '');
    const cards = resultsContainer ? Array.from(resultsContainer.querySelectorAll('.business-card')) : [];
    let any=false;
    cards.forEach(card=>{
      const searchableText = sanitizeText(card.textContent);
      const matches = (q ? searchableText.includes(q) : true)
        && (!st || sanitizeText(card.dataset.state).includes(st))
        && (!ct || sanitizeText(card.dataset.city).includes(ct));
      card.style.display = matches ? '' : 'none';
      if(matches) any=true;
    });
    const nr = resultsContainer ? resultsContainer.querySelector('.no-results') : null;
    if(nr) nr.style.display = any ? 'none' : '';
    const resultCount = document.getElementById('resultCount');
    if (resultCount) resultCount.textContent = `${cards.filter(card => card.style.display !== 'none').length} heritage businesses found`;
  }

  function populateFilters(){
    if(!resultsContainer) return;
    const cards = Array.from(resultsContainer.querySelectorAll('.business-card'));
    const states = new Set(), cities = new Set();
    cards.forEach(card=>{
      if(card.dataset.state) states.add(card.dataset.state);
      if(card.dataset.city) cities.add(card.dataset.city);
    });
    function fill(selectEl, items){
      if(!selectEl) return;
      // remove existing except first
      while(selectEl.options.length>1) selectEl.remove(1);
      Array.from(items).sort().forEach(it=>{
        const o = document.createElement('option'); o.value = it; o.textContent = it; selectEl.appendChild(o);
      });
    }
    fill(stateFilter, states);
    fill(cityFilter, cities);
  }

  function ensureImageAccessibility(){
    document.querySelectorAll('img').forEach(img=>{
      if(!img.getAttribute('alt') || img.getAttribute('alt').trim()===''){
        const title = img.getAttribute('title') || img.getAttribute('data-title') || 'Business image';
        img.setAttribute('alt', title);
      }
      if(!img.getAttribute('title')){
        const alt = img.getAttribute('alt') || 'Business image';
        img.setAttribute('title', alt);
      }
    });
    document.querySelectorAll('img[data-fallback-image]').forEach(img => {
      img.addEventListener('error', () => { img.src = img.dataset.fallbackImage; }, { once: true });
    });
  }

  function initializeGallery(){
    const featuredImage = document.querySelector('.gallery-featured');
    const thumbnails = Array.from(document.querySelectorAll('.gallery-thumbnail-button'));
    const previousButton = document.querySelector('.gallery-previous');
    const nextButton = document.querySelector('.gallery-next');

    if(!featuredImage || thumbnails.length === 0) return;

    let currentIndex = 0;

    const showImage = (index) => {
      currentIndex = (index + thumbnails.length) % thumbnails.length;
      const thumbnailImage = thumbnails[currentIndex].querySelector('img');
      if(!thumbnailImage) return;

      featuredImage.src = thumbnailImage.src;
      thumbnails.forEach((thumbnail, thumbnailIndex) => {
        thumbnail.classList.toggle('active', thumbnailIndex === currentIndex);
        thumbnail.setAttribute('aria-current', thumbnailIndex === currentIndex ? 'true' : 'false');
      });
    };

    thumbnails.forEach((thumbnail, index) => {
      thumbnail.addEventListener('click', () => showImage(index));
    });
    previousButton?.addEventListener('click', () => showImage(currentIndex - 1));
    nextButton?.addEventListener('click', () => showImage(currentIndex + 1));
    showImage(0);
  }

  async function markVisitedBusinesses(){
    const cards = Array.from(document.querySelectorAll('.business-card[data-business-id]'));
    if(cards.length === 0) return;
    try {
      const response = await fetch('/api/visits/business-ids');
      if(!response.ok) throw new Error('Unable to load check-in history');
      const visitedIds = new Set(await response.json());
      cards.forEach(card => {
        const visited = visitedIds.has(card.dataset.businessId);
        card.classList.toggle('business-visited', visited);
        const label = card.querySelector('.business-visited-label');
        if(label) label.hidden = !visited;
      });
    } catch(error) {
      console.warn('Unable to label checked-in businesses:', error);
    }
  }

  // Event wiring
  if(searchInput) searchInput.addEventListener('input', applyFilters);
  if(searchButton) searchButton.addEventListener('click', applyFilters);
  if(clearBtn) clearBtn.addEventListener('click', ()=>{ if(searchInput) searchInput.value=''; if(stateFilter) stateFilter.value=''; if(cityFilter) cityFilter.value=''; applyFilters(); });
  [stateFilter, cityFilter].forEach(el=>{ if(el) el.addEventListener('change', applyFilters); });

  // initialize on DOM ready
  document.addEventListener('DOMContentLoaded', ()=>{
    populateFilters();
    ensureImageAccessibility();
    initializeGallery();
    markVisitedBusinesses();
    applyFilters();
  });

})();
