document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("processingForm");
    const processButton = document.getElementById("processButton");

    const resultSection = document.getElementById("resultSection");
    const statusMessage = document.getElementById("statusMessage");

    const audioStatus = document.getElementById("audioStatus");
    const transcriptionStatus = document.getElementById("transcriptionStatus");
    const extractionStatus = document.getElementById("extractionStatus");

    const audioFileContainer = document.getElementById("audioFileContainer");
    const transcriptContainer = document.getElementById("transcriptContainer");
    const jsonContainer = document.getElementById("jsonContainer");

    const audioFile = document.getElementById("audioFile");
    const transcript = document.getElementById("transcript");
    const jsonResult = document.getElementById("jsonResult");

    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        const videoUrl = document.getElementById("videoUrl").value.trim();

        if (!videoUrl) {
            updateStatus("Please enter a YouTube URL.", "danger");
            resultSection.classList.remove("d-none");
            return;
        }

        resetResults();

        resultSection.classList.remove("d-none");
        processButton.disabled = true;
        processButton.innerText = "Processing...";

        try {

            // =====================================================
            // STEP 1 : Audio Extraction
            // =====================================================

            updateStatus("Step 1: Extracting audio...", "info");
            audioStatus.innerText = "Downloading using yt-dlp...";

            const audioResponse = await fetch("/api/audio/test", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    videoUrl
                })
            });

            if (!audioResponse.ok) {
                throw new Error(await getErrorMessage(audioResponse));
            }

            const audioData = await audioResponse.json();

            const audioPath = audioData.audioFile || audioData.file;

            audioFile.value = audioPath;
            audioFileContainer.classList.remove("d-none");

            audioStatus.innerText = "Completed";

            // =====================================================
            // STEP 2 : Speech To Text
            // =====================================================

            updateStatus("Step 2: Transcribing audio...", "info");
            transcriptionStatus.innerText = "AssemblyAI is processing...";

            const sttResponse = await fetch("/api/transcription/test", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    audioFile: audioPath
                })
            });

            if (!sttResponse.ok) {
                throw new Error(await getErrorMessage(sttResponse));
            }

            const sttData = await sttResponse.json();

            transcript.value = sttData.transcript;
            transcriptContainer.classList.remove("d-none");

            transcriptionStatus.innerText = "Completed";

            // =====================================================
            // STEP 3 : Gemini Extraction
            // =====================================================

            updateStatus("Step 3: Extracting heritage information...", "info");
            extractionStatus.innerText = "Gemini is analysing transcript...";

            const aiResponse = await fetch("/api/ai-extraction/extract", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    transcript: sttData.transcript
                })
            });

            if (!aiResponse.ok) {
                throw new Error(await getErrorMessage(aiResponse));
            }

            const aiData = await aiResponse.json();

            jsonResult.textContent = JSON.stringify(aiData, null, 2);
            jsonContainer.classList.remove("d-none");

            extractionStatus.innerText = "Completed";

            updateStatus("All processing completed successfully.", "success");

        } catch (error) {

            console.error(error);

            updateStatus(
                error.message || "Processing failed.",
                "danger"
            );

        } finally {

            processButton.disabled = false;
            processButton.innerText = "Process Video";

        }

    });

    // =========================================================
    // Helper Functions
    // =========================================================

    function resetResults() {

        audioStatus.innerText = "Waiting...";
        transcriptionStatus.innerText = "Waiting...";
        extractionStatus.innerText = "Waiting...";

        if (audioFile) audioFile.value = "";
        if (transcript) transcript.value = "";
        if (jsonResult) jsonResult.textContent = "";

        if (audioFileContainer)
            audioFileContainer.classList.add("d-none");

        if (transcriptContainer)
            transcriptContainer.classList.add("d-none");

        if (jsonContainer)
            jsonContainer.classList.add("d-none");

        updateStatus("Waiting...", "info");
    }

    function updateStatus(message, type) {

        statusMessage.className = `alert alert-${type}`;
        statusMessage.innerText = message;

    }

    async function getErrorMessage(response) {

        try {

            const data = await response.json();

            return data.message || data.error || `Error ${response.status}`;

        } catch {

            return `Error ${response.status}`;

        }
    }

});