// Business Heritage JS — handles search, filters and accessibility tweaks
(function(){
  const searchInput = document.getElementById('searchInput');
  const searchBtn = document.getElementById('searchBtn');
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
    const cards = resultsContainer ? Array.from(resultsContainer.querySelectorAll('.card')) : [];
    let any=false;
    cards.forEach(card=>{
      const name = sanitizeText(card.querySelector('h3')?.textContent || '');
      const meta = sanitizeText(card.querySelector('.meta')?.textContent || '');
      const matches = (q ? name.includes(q) : true) && (!cat || meta.includes(cat)) && (!st || meta.includes(st)) && (!ct || meta.includes(ct));
      card.style.display = matches ? '' : 'none';
      if(matches) any=true;
    });
    const nr = resultsContainer ? resultsContainer.querySelector('.no-results') : null;
    if(nr) nr.style.display = any ? 'none' : '';
  }

  function populateFilters(){
    if(!resultsContainer) return;
    const cards = Array.from(resultsContainer.querySelectorAll('.card'));
    const cats = new Set(), states = new Set(), cities = new Set();
    cards.forEach(card=>{
      const meta = (card.querySelector('.meta')?.textContent || '');
      const parts = meta.split('•').map(s=>s.trim());
      if(parts[0]) cats.add(parts[0]);
      if(parts[1]){
        const locPart = parts[1].split(',').map(s=>s.trim());
        if(locPart[0]) cities.add(locPart[0]);
        if(locPart[1]) states.add(locPart[1]);
      }
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
    if(!resultsContainer) return;
    resultsContainer.querySelectorAll('img').forEach(img=>{
      if(!img.getAttribute('alt') || img.getAttribute('alt').trim()===''){
        const title = img.getAttribute('title') || img.getAttribute('data-title') || 'Business image';
        img.setAttribute('alt', title);
      }
      if(!img.getAttribute('title')){
        const alt = img.getAttribute('alt') || 'Business image';
        img.setAttribute('title', alt);
      }
    });
  }

  // Event wiring
  if(searchBtn) searchBtn.addEventListener('click', applyFilters);
  if(searchInput) searchInput.addEventListener('keyup', function(e){ if(e.key==='Enter') applyFilters(); });
  if(clearBtn) clearBtn.addEventListener('click', ()=>{ if(searchInput) searchInput.value=''; if(categoryFilter) categoryFilter.value=''; if(stateFilter) stateFilter.value=''; if(cityFilter) cityFilter.value=''; applyFilters(); });
  [categoryFilter, stateFilter, cityFilter].forEach(el=>{ if(el) el.addEventListener('change', applyFilters); });

  // initialize on DOM ready
  document.addEventListener('DOMContentLoaded', ()=>{
    populateFilters();
    ensureImageAccessibility();
  });

})();
