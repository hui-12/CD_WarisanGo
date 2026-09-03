document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('business-report-search');
  const categorySelect = document.getElementById('business-report-category');
  const rows = [...document.querySelectorAll('.report-table-row')];
  const noResults = document.getElementById('business-report-no-results');
  if (!searchInput || !categorySelect || rows.length === 0) return;

  const filterReports = () => {
    const query = searchInput.value.trim().toLocaleLowerCase();
    const category = categorySelect.value;
    let visibleCount = 0;

    rows.forEach((row) => {
      const matchesSearch = !query || row.textContent.toLocaleLowerCase().includes(query);
      const matchesCategory = category === 'ALL' || row.dataset.reportCategory === category;
      const visible = matchesSearch && matchesCategory;
      row.hidden = !visible;
      if (visible) visibleCount += 1;
    });

    if (noResults) noResults.hidden = visibleCount !== 0;
  };

  searchInput.addEventListener('input', filterReports);
  categorySelect.addEventListener('change', filterReports);
});
