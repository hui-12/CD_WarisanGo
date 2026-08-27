// Business Heritage JS — handles search, filters and accessibility tweaks
(function(){
  const searchInput = document.getElementById('searchInput');
  const categoryFilter = document.getElementById('categoryFilter');
  const stateFilter = document.getElementById('stateFilter');
  const cityFilter = document.getElementById('cityFilter');
  const clearBtn = document.getElementById('clearFilters');
  const resultsContainer = document.getElementById('results');

  function sanitizeText(s){ return (s||'').toString().toLowerCase(); }

  function applyFilters(){
    const q = sanitizeText(searchInput?.value || '');
    const cat = sanitizeText(categoryFilter?.value || '');
    const st = sanitizeText(stateFilter?.value || '');
    const ct = sanitizeText(cityFilter?.value || '');
    const cards = resultsContainer ? Array.from(resultsContainer.querySelectorAll('.business-card')) : [];
    let any=false;
    cards.forEach(card=>{
      const name = sanitizeText(card.querySelector('h3')?.textContent || '');
      const matches = (q ? name.includes(q) : true)
        && (!cat || sanitizeText(card.dataset.category).includes(cat))
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
    const cats = new Set(), states = new Set(), cities = new Set();
    cards.forEach(card=>{
      if(card.dataset.category) cats.add(card.dataset.category);
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
    fill(categoryFilter, cats);
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

  function initializeGallery() {
    const featuredImage = document.getElementById('galleryFeatured');
    const thumbnailButtons = Array.from(document.querySelectorAll('.gallery-thumbnail'));
    const galleryStage = document.querySelector('.gallery-stage');
    if (!featuredImage || thumbnailButtons.length < 2 || !galleryStage) return;

    const photoUrls = thumbnailButtons.map(button => button.querySelector('img')?.src).filter(Boolean);
    let activeIndex = 0;

    const showPhoto = (nextIndex) => {
      activeIndex = (nextIndex + photoUrls.length) % photoUrls.length;
      featuredImage.src = photoUrls[activeIndex];
      thumbnailButtons.forEach((button, index) => {
        button.setAttribute('aria-current', String(index === activeIndex));
      });
    };

    document.querySelector('[data-gallery-previous]')?.addEventListener('click', () => showPhoto(activeIndex - 1));
    document.querySelector('[data-gallery-next]')?.addEventListener('click', () => showPhoto(activeIndex + 1));
    thumbnailButtons.forEach((button, index) => button.addEventListener('click', () => showPhoto(index)));
    galleryStage.addEventListener('keydown', (event) => {
      if (event.key === 'ArrowLeft') {
        event.preventDefault();
        showPhoto(activeIndex - 1);
      }
      if (event.key === 'ArrowRight') {
        event.preventDefault();
        showPhoto(activeIndex + 1);
      }
    });
  }

  // Event wiring
  if(searchInput) searchInput.addEventListener('input', applyFilters);
  if(clearBtn) clearBtn.addEventListener('click', ()=>{ if(searchInput) searchInput.value=''; if(categoryFilter) categoryFilter.value=''; if(stateFilter) stateFilter.value=''; if(cityFilter) cityFilter.value=''; applyFilters(); });
  [categoryFilter, stateFilter, cityFilter].forEach(el=>{ if(el) el.addEventListener('change', applyFilters); });

  // initialize on DOM ready
  document.addEventListener('DOMContentLoaded', ()=>{
    populateFilters();
    ensureImageAccessibility();
    initializeGallery();
    applyFilters();
  });

})();
