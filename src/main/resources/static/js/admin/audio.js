(function (window) {
    "use strict";

    async function extractAudio(videoUrl) {
        if (!videoUrl) throw new Error("YouTube video URL is required.");

        const response = await fetch("/api/audio/test", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ videoUrl: videoUrl })
        });
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "Audio extraction failed.");
        }
        if (!data.audioFile || !data.audioFile.trim()) {
            throw new Error("Audio file path was not returned by the server.");
        }
        return data.audioFile;
    }

    window.WarisanGoAudio = { extract: extractAudio };
})(window);
