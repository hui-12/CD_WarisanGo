const audioTestForm = document.getElementById("audioTestForm");
const videoUrlInput = document.getElementById("videoUrl");
const testButton = document.getElementById("testButton");

const resultSection = document.getElementById("resultSection");
const statusMessage = document.getElementById("statusMessage");

const audioUrlContainer =
    document.getElementById("audioUrlContainer");

const audioUrl =
    document.getElementById("audioUrl");


audioTestForm.addEventListener("submit", async (event) => {

    event.preventDefault();

    const videoUrl = videoUrlInput.value.trim();

    if (!videoUrl) {
        showError("Please enter a YouTube URL.");
        return;
    }

    setLoading(true);

    try {

        const response = await fetch("/api/audio/test", {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                videoUrl: videoUrl
            })
        });

        const data = await response.json();

        if (!response.ok) {

            showError(
                data.message || "Audio extraction failed."
            );

            return;
        }

        showSuccess("Audio URL retrieved successfully.");

        audioUrl.value = data.audioUrl;

        audioUrlContainer.classList.remove("d-none");

    } catch (error) {

        showError(
            "Unable to connect to the Spring Boot server."
        );

        console.error(error);

    } finally {

        setLoading(false);
    }
});


function showSuccess(message) {

    resultSection.classList.remove("d-none");

    statusMessage.className =
        "alert alert-success";

    statusMessage.textContent = message;
}


function showError(message) {

    resultSection.classList.remove("d-none");

    statusMessage.className =
        "alert alert-danger";

    statusMessage.textContent = message;

    audioUrlContainer.classList.add("d-none");
}


function setLoading(isLoading) {

    testButton.disabled = isLoading;

    if (isLoading) {

        testButton.textContent =
            "Extracting Audio...";

    } else {

        testButton.textContent =
            "Get Audio URL";
    }
}

async function transcribeAudio() {

    const audioUrl = document.getElementById("audioUrl").value;

    if (!audioUrl) {
        alert("Please enter an audio URL.");
        return;
    }

    try {
        const response = await fetch("/api/transcription/test", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                audioUrl: audioUrl
            })
        });

        if (!response.ok) {
            throw new Error("Transcription request failed.");
        }

        const data = await response.json();

        document.getElementById("transcript").textContent =
            data.transcript;

    } catch (error) {
        console.error(error);
        alert("Failed to transcribe audio.");
    }
}