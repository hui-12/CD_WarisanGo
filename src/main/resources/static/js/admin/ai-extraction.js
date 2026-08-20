(function (window) {
    "use strict";

    async function extractHeritage(transcript) {
        if (!transcript) throw new Error("Transcript is required.");

        const response = await fetch("/api/ai-extraction/extract", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ transcript: transcript })
        });
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "AI extraction failed.");
        }
        return data;
    }

    window.WarisanGoAIExtraction = { extract: extractHeritage };
})(window);
