document.addEventListener("DOMContentLoaded", () => {
  const filters = [...document.querySelectorAll(".status-filter")];
  const summary = document.querySelector("#audit-search-summary");
  const noResults = document.querySelector("#audit-no-results");
  const rows = [...document.querySelectorAll(".audit-entry")];
  if (!filters.length || !summary || !noResults) return;

  const filterRows = (selectedStatus) => {
    const visibleRows = rows.filter((row) => {
      const matches = selectedStatus === "ALL" || row.dataset.status === selectedStatus;
      row.hidden = !matches;
      return matches;
    });
    filters.forEach((button) => {
      button.setAttribute("aria-pressed", String(button.dataset.status === selectedStatus));
    });
    noResults.hidden = visibleRows.length !== 0 || rows.length === 0;
    summary.textContent = `${visibleRows.length} of ${rows.length} businesses shown`;
  };
  filters.forEach((button) => {
    button.addEventListener("click", () => filterRows(button.dataset.status));
  });
  filterRows("ALL");
});