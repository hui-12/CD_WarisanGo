document.addEventListener("DOMContentLoaded", () => {
  const input = document.querySelector("#processed-business-search");
  const statusSelect = document.querySelector("#processed-business-status");
  const clearButton = document.querySelector("#processed-business-clear");
  const summary = document.querySelector("#audit-search-summary");
  const noResults = document.querySelector("#audit-no-results");
  const rows = [...document.querySelectorAll(".audit-entry")];

  if (!input || !statusSelect || !clearButton || !summary || !noResults) return;

  const filterRows = () => {
    const query = input.value.trim().toLocaleLowerCase();
    const selectedStatus = statusSelect.value;
    let visibleCount = 0;
    rows.forEach((row) => {
      const matchesSearch = !query || (row.dataset.search || "").toLocaleLowerCase().includes(query);
      const matchesStatus = selectedStatus === "ALL" || row.dataset.status === selectedStatus;
      const matches = matchesSearch && matchesStatus;
      row.hidden = !matches;
      if (matches) visibleCount += 1;
    });
    const hasFilters = Boolean(query) || selectedStatus !== "ALL";
    clearButton.hidden = !hasFilters;
    noResults.hidden = visibleCount !== 0 || rows.length === 0;
    summary.textContent = hasFilters ? `${visibleCount} of ${rows.length} businesses shown` : "";
  };

  input.addEventListener("input", filterRows);
  statusSelect.addEventListener("change", filterRows);
  clearButton.addEventListener("click", () => {
    input.value = "";
    statusSelect.value = "ALL";
    filterRows();
    input.focus();
  });
});
