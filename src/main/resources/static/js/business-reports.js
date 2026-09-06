document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('business-report-search');
  const statusSelect = document.getElementById('business-report-status');
  const typeSelect = document.getElementById('business-report-type');
  const clearButton = document.getElementById('business-report-clear');
  const summary = document.getElementById('business-report-filter-summary');
  const rows = [...document.querySelectorAll('.moderation-report-card')];
  const noResults = document.getElementById('business-report-no-results');
  const correctionSection = document.getElementById('correction-reports');
  const correctionHeading = document.getElementById('correction-reports-heading');
  const photoSection = document.getElementById('photo-reports');
  if (!searchInput || !statusSelect || !typeSelect || !clearButton) return;

  const statusesByType = {
    ALL: [
      ['PENDING_REVIEW', 'Pending Review'],
      ['RESOLVED', 'Resolved'],
      ['DISMISSED', 'Dismissed'],
      ['PENDING', 'Pending'],
      ['REMOVED', 'Removed'],
    ],
    CORRECTION: [
      ['PENDING_REVIEW', 'Pending Review'],
      ['RESOLVED', 'Resolved'],
      ['DISMISSED', 'Dismissed'],
    ],
    PHOTO: [
      ['PENDING', 'Pending'],
      ['DISMISSED', 'Dismissed'],
      ['REMOVED', 'Removed'],
    ],
  };

  const normalize = (value) => String(value || '').trim().toLocaleLowerCase();

  const filterReports = () => {
    const queryTerms = normalize(searchInput.value).split(/\s+/).filter(Boolean);
    const status = String(statusSelect.value || 'ALL').trim().toUpperCase();
    const reportType = String(typeSelect.value || 'ALL').trim().toUpperCase();
    let visibleCount = 0;

    rows.forEach((row) => {
      const searchableText = normalize(row.dataset.reportSearch || row.textContent);
      const reportStatus = String(row.dataset.reportStatus || '').trim().toUpperCase();
      const rowType = String(row.dataset.reportType || '').trim().toUpperCase();
      const matchesSearch = queryTerms.every((term) => searchableText.includes(term));
      const matchesStatus = status === 'ALL' || reportStatus === status;
      const matchesType = reportType === 'ALL' || rowType === reportType;
      const visible = matchesSearch && matchesStatus && matchesType;
      row.hidden = !visible;
      if (visible) visibleCount += 1;
    });

    document.querySelectorAll('.photo-report-status-group').forEach((group) => {
      const hasVisibleReport = [...group.querySelectorAll('.moderation-report-card')]
        .some((report) => !report.hidden);
      group.hidden = !hasVisibleReport;
    });
    const showCorrections = reportType !== 'PHOTO';
    const showPhotos = reportType !== 'CORRECTION';
    if (correctionSection) correctionSection.hidden = !showCorrections;
    if (correctionHeading) correctionHeading.hidden = !showCorrections;
    if (photoSection) photoSection.hidden = !showPhotos;

    if (noResults) noResults.hidden = visibleCount !== 0;
    const hasFilters = queryTerms.length > 0 || status !== 'ALL' || reportType !== 'ALL';
    clearButton.hidden = !hasFilters;
    if (summary) {
      summary.textContent = hasFilters ? `${visibleCount} of ${rows.length} reports shown` : '';
    }
  };

  searchInput.addEventListener('input', filterReports);
  statusSelect.addEventListener('change', filterReports);
  typeSelect.addEventListener('change', () => {
    const selectedType = String(typeSelect.value || 'ALL').toUpperCase();
    statusSelect.replaceChildren(new Option('All statuses', 'ALL'));
    statusesByType[selectedType].forEach(([value, label]) => statusSelect.add(new Option(label, value)));
    filterReports();
  });
  clearButton.addEventListener('click', () => {
    searchInput.value = '';
    statusSelect.value = 'ALL';
    typeSelect.value = 'ALL';
    filterReports();
    searchInput.focus();
  });

  filterReports();
});
