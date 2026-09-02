(function (window) {
    "use strict";

    async function startJob(videoUrl) {
        const response = await fetch("/api/discovery/process/jobs", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ videoUrl: videoUrl })
        });
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "Video processing failed.");
        }

        return data.jobId;
    }

    async function getJob(jobId) {
        const response = await fetch("/api/discovery/process/jobs/" + encodeURIComponent(jobId));
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "Unable to read processing status.");
        }

        return data;
    }

    window.WarisanGoWorkflow = { start: startJob, get: getJob };
})(window);
