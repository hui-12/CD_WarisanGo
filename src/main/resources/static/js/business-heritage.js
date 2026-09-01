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
    if (clearBtn) clearBtn.style.display = q || st || ct ? 'block' : '';
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

  function populateCitiesForState(){
    if(!resultsContainer || !cityFilter) return;
    const selectedCity = cityFilter.value;
    const selectedState = sanitizeText(stateFilter?.value || '');
    const cities = new Set();
    Array.from(resultsContainer.querySelectorAll('.business-card')).forEach(card=>{
      if((!selectedState || sanitizeText(card.dataset.state) === selectedState) && card.dataset.city){
        cities.add(card.dataset.city);
      }
    });
    while(cityFilter.options.length>1) cityFilter.remove(1);
    Array.from(cities).sort().forEach(city=>cityFilter.add(new Option(city, city)));
    if(Array.from(cityFilter.options).some(option=>option.value===selectedCity)) cityFilter.value=selectedCity;
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

  function setupGallery(){
    const stage = document.querySelector('.gallery-stage');
    const featured = document.getElementById('galleryFeatured');
    const thumbnails = Array.from(document.querySelectorAll('.gallery-thumbnail'));
    if (!stage || !featured || thumbnails.length < 2) return;

    const photos = thumbnails.map(thumbnail => thumbnail.querySelector('img')?.src).filter(Boolean);
    if (photos.length < 2) return;

    let selectedIndex = 0;
    const showPhoto = (index) => {
      selectedIndex = (index + photos.length) % photos.length;
      featured.src = photos[selectedIndex];
      const photoTitle = thumbnails[selectedIndex].querySelector('img')?.alt || 'Business photo';
      featured.alt = photoTitle;
      featured.title = photoTitle;
      thumbnails.forEach((thumbnail, thumbnailIndex) => {
        const selected = thumbnailIndex === selectedIndex;
        thumbnail.classList.toggle('is-active', selected);
        thumbnail.setAttribute('aria-current', selected ? 'true' : 'false');
      });
    };

    document.querySelector('[data-gallery-previous]')?.addEventListener('click', () => showPhoto(selectedIndex - 1));
    document.querySelector('[data-gallery-next]')?.addEventListener('click', () => showPhoto(selectedIndex + 1));
    thumbnails.forEach((thumbnail, index) => thumbnail.addEventListener('click', () => showPhoto(index)));

    // The left and right halves of the photo act as previous and next controls.
    stage.addEventListener('click', (event) => {
      if (event.target.closest('.gallery-nav')) return;
      const bounds = stage.getBoundingClientRect();
      showPhoto(event.clientX - bounds.left < bounds.width / 2 ? selectedIndex - 1 : selectedIndex + 1);
    });
    stage.addEventListener('keydown', (event) => {
      if (event.key === 'ArrowLeft') {
        event.preventDefault();
        showPhoto(selectedIndex - 1);
      }
      if (event.key === 'ArrowRight') {
        event.preventDefault();
        showPhoto(selectedIndex + 1);
      }
    });

    showPhoto(0);
  }

  // Event wiring
  if(searchInput) searchInput.addEventListener('input', applyFilters);
  if(searchButton) searchButton.addEventListener('click', applyFilters);
  if(clearBtn) clearBtn.addEventListener('click', ()=>{
    if(searchInput) searchInput.value='';
    if(stateFilter) stateFilter.value='';
    populateCitiesForState();
    if(cityFilter) cityFilter.value='';
    applyFilters();
  });
  if(stateFilter) stateFilter.addEventListener('change', ()=>{ populateCitiesForState(); applyFilters(); });
  if(cityFilter) cityFilter.addEventListener('change', applyFilters);

  // initialize on DOM ready
  document.addEventListener('DOMContentLoaded', ()=>{
    populateFilters();
    ensureImageAccessibility();
    applyFilters();
    setupGallery();
  });

})();
