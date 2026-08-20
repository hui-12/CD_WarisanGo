const transcriptionTestForm =
    document.getElementById("transcriptionTestForm");

const audioUrlInput =
    document.getElementById("audioUrl");

const transcribeButton =
    document.getElementById("transcribeButton");

const resultSection =
    document.getElementById("resultSection");

const statusMessage =
    document.getElementById("statusMessage");

const transcript =
    document.getElementById("transcript");


transcriptionTestForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const audioUrl =
            audioUrlInput.value.trim();

        if (!audioUrl) {

            showError(
                "Please enter an audio URL."
            );

            return;
        }

        setLoading(true);

        try {

            const response = await fetch(
                "/api/transcription/test",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        audioUrl: audioUrl
                    })
                }
            );

            const data =
                await response.json();

            if (!response.ok) {

                showError(
                    data.message ||
                    "Transcription failed."
                );

                return;
            }

            showSuccess(
                "Transcription completed successfully."
            );

            transcript.value =
                data.transcript || "No transcript returned.";

        } catch (error) {

            console.error(error);

            showError(
                "Unable to connect to the Spring Boot server."
            );

        } finally {

            setLoading(false);
        }
    }
);


function showSuccess(message) {

    resultSection.classList.remove("d-none");

    statusMessage.className =
        "alert alert-success";

    statusMessage.textContent =
        message;
}


function showError(message) {

    resultSection.classList.remove("d-none");

    statusMessage.className =
        "alert alert-danger";

    statusMessage.textContent =
        message;

    transcript.value = "";
}


function setLoading(isLoading) {

    transcribeButton.disabled =
        isLoading;

    transcribeButton.textContent =
        isLoading
            ? "Transcribing..."
            : "Transcribe Audio";
}