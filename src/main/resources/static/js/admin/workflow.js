(function (window) {
    "use strict";

    async function processVideo(videoUrl) {
        const response = await fetch("/api/discovery/process", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ videoUrl: videoUrl })
        });
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "Video processing failed.");
        }

        return data;
    }

    window.WarisanGoWorkflow = { process: processVideo };
})(window);
