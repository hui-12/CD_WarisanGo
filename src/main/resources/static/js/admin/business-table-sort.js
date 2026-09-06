document.addEventListener("DOMContentLoaded", () => {
  const collator = new Intl.Collator(undefined, { sensitivity: "base", numeric: true });

  document.querySelectorAll(".business-table").forEach((table) => {
    const body = table.tBodies[0];
    const buttons = [...table.querySelectorAll(".table-sort")];
    if (!body) return;

    buttons.forEach((button) => {
      button.addEventListener("click", () => {
        const header = button.closest("th");
        const ascending = header.getAttribute("aria-sort") !== "ascending";
        const direction = ascending ? 1 : -1;
        const rows = [...body.rows];

        rows.sort((first, second) => {
          if (button.dataset.sort === "date") {
            const firstDate = Date.parse(first.querySelector("time")?.getAttribute("datetime") || "");
            const secondDate = Date.parse(second.querySelector("time")?.getAttribute("datetime") || "");
            // Missing dates remain last in either direction.
            if (Number.isNaN(firstDate)) return Number.isNaN(secondDate) ? 0 : 1;
            if (Number.isNaN(secondDate)) return -1;
            return (firstDate - secondDate) * direction;
          }
          return collator.compare(
            first.querySelector(".business-title-link").textContent.trim(),
            second.querySelector(".business-title-link").textContent.trim()
          ) * direction;
        });

        rows.forEach((row) => body.appendChild(row));
        buttons.forEach((other) => {
          other.closest("th").setAttribute("aria-sort", "none");
          other.querySelector(".sort-direction").textContent = "↕";
        });
        header.setAttribute("aria-sort", ascending ? "ascending" : "descending");
        button.querySelector(".sort-direction").textContent = ascending ? "↑" : "↓";
      });
    });
  });
});
