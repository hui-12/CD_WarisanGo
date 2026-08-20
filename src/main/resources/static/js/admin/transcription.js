(function (window) {
    "use strict";

    async function transcribeAudio(audioFile) {
        if (!audioFile) throw new Error("Audio file path is required.");

        const response = await fetch("/api/transcription/test", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ audioFile: audioFile })
        });
        const data = await response.json().catch(function () { return {}; });

        if (!response.ok) {
            throw new Error(data.message || data.error || "Transcription failed.");
        }
        if (!data.transcript || !data.transcript.trim()) {
            throw new Error("No transcript was returned by AssemblyAI.");
        }
        return data.transcript;
    }

    window.WarisanGoTranscription = { transcribe: transcribeAudio };
})(window);
