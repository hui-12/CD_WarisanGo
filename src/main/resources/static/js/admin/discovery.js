document.addEventListener("DOMContentLoaded", function () {

    const keywordInput =
        document.getElementById("keyword");

    const searchButton =
        document.getElementById("searchButton");

    const searchStatus =
        document.getElementById("searchStatus");

    const videoResultsSection =
        document.getElementById("videoResultsSection");

    const videoList =
        document.getElementById("videoList");

    const processingSection =
        document.getElementById("processingSection");

    const processingStatus =
        document.getElementById("processingStatus");

    const selectedVideoTitle =
        document.getElementById("selectedVideoTitle");

    const selectedVideoUrl =
        document.getElementById("selectedVideoUrl");

    const audioStatus =
        document.getElementById("audioStatus");

    const transcriptionStatus =
        document.getElementById("transcriptionStatus");

    const extractionStatus =
        document.getElementById("extractionStatus");

    const transcriptSection =
        document.getElementById("transcriptSection");

    const transcript =
        document.getElementById("transcript");

    const resultSection =
        document.getElementById("resultSection");

    const jsonResult =
        document.getElementById("jsonResult");

    const copyJsonButton =
        document.getElementById("copyJsonButton");

    // SEARCH
    searchButton.addEventListener(
        "click",
        searchVideos
    );

    keywordInput.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Enter") {

                event.preventDefault();

                searchVideos();
            }
        }
    );

    async function searchVideos() {

        const keyword =
            keywordInput.value.trim();

        if (!keyword) {

            showSearchStatus(
                "Please enter a search keyword.",
                "danger"
            );

            return;
        }

        resetPage();

        searchButton.disabled = true;

        searchButton.innerText =
            "Searching...";

        showSearchStatus(
            "Searching YouTube videos...",
            "info"
        );

        try {

            const response =
                await fetch(
                    "/api/discovery/search",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            keyword: keyword
                        })
                    }
                );

            if (!response.ok) {

                throw new Error(
                    await getErrorMessage(response)
                );
            }

            const videos =
                await response.json();

            if (!Array.isArray(videos)
                || videos.length === 0) {

                showSearchStatus(
                    "No videos were found.",
                    "warning"
                );

                return;
            }

            displayVideos(videos);

            showSearchStatus(
                videos.length
                + " video(s) found. Click Process Video to continue.",
                "success"
            );

        } catch (error) {

            console.error(
                "Video search failed:",
                error
            );

            showSearchStatus(
                error.message
                || "Unable to search YouTube.",
                "danger"
            );

        } finally {

            searchButton.disabled = false;

            searchButton.innerText =
                "Search Videos";
        }
    }

    // DISPLAY VIDEOS
    function displayVideos(videos) {

        videoList.innerHTML = "";

        videos.forEach(function (video) {

            const column =
                document.createElement("div");

            column.className =
                "col-md-6";

            const card =
                document.createElement("div");

            card.className =
                "card video-card";

            const thumbnail =
                document.createElement("img");

            thumbnail.className =
                "video-thumbnail";

            thumbnail.src =
                video.thumbnail;

            thumbnail.alt =
                video.title;

            const cardBody =
                document.createElement("div");

            cardBody.className =
                "video-card-body";

            const title =
                document.createElement("div");

            title.className =
                "video-title";

            title.textContent =
                video.title;

            const channel =
                document.createElement("div");

            channel.className =
                "video-channel";

            channel.textContent =
                video.channel;

            const description =
                document.createElement("div");

            description.className =
                "video-description";

            description.textContent =
                video.description;

            const processButton =
                document.createElement("button");

            processButton.type =
                "button";

            processButton.className =
                "btn btn-success mt-3";

            processButton.innerText =
                "Process Video";

            processButton.addEventListener(
                "click",
                function () {

                    processVideo(video, processButton);
                }
            );

            cardBody.appendChild(title);

            cardBody.appendChild(channel);

            cardBody.appendChild(description);

            cardBody.appendChild(processButton);

            card.appendChild(thumbnail);

            card.appendChild(cardBody);

            column.appendChild(card);

            videoList.appendChild(column);

        });

        videoResultsSection.classList.remove(
            "d-none"
        );
    }

    // PROCESS VIDEO
    async function processVideo(video, processButton) {

        const videoUrl =
            "https://www.youtube.com/watch?v="
            + video.videoId;

        processingSection.classList.remove(
            "d-none"
        );

        transcriptSection.classList.add(
            "d-none"
        );


        resultSection.classList.add(
            "d-none"
        );

        selectedVideoTitle.textContent =
            video.title;


        selectedVideoUrl.textContent =
            videoUrl;

        resetWorkflow();

        processingStatus.className =
            "alert alert-info";

        processingStatus.innerText =
            "Starting AI Heritage Discovery...";

        processButton.disabled = true;
        processButton.innerText = "Processing...";

        try {

            // STEP 1 YouTube → VideoAudioService

            audioStatus.innerText =
                "Server is downloading the selected video audio...";

            transcriptionStatus.innerText =
                "Waiting for server-side transcription...";

            extractionStatus.innerText =
                "Waiting for Gemini extraction...";

            // STEP 2 Audio → AssemblyAI
            processingStatus.innerText =
                "Processing selected video on the server...";

            const workflowResult =
                await window.WarisanGoWorkflow.process(videoUrl);

            const transcriptText =
                workflowResult.transcript;

            const extractionData =
                workflowResult.extraction;

            transcript.value =
                transcriptText;

            transcriptSection.classList.remove(
                "d-none"
            );

            transcriptionStatus.innerText =
                "Transcription completed.";

            // STEP 3 Transcript → Gemini
            audioStatus.innerText =
                "Audio extraction completed.";

            // =====================================
            // DISPLAY GEMINI RESULT
            // =====================================

            jsonResult.textContent =
                JSON.stringify(
                    extractionData,
                    null,
                    2
                );

            resultSection.classList.remove(
                "d-none"
            );

            extractionStatus.innerText =
                "AI extraction completed.";

            processingStatus.className =
                "alert alert-success";

            processingStatus.innerText =
                "AI Heritage Discovery completed successfully.";

        } catch (error) {

            console.error(
                "AI processing failed:",
                error
            );

            processingStatus.className =
                "alert alert-danger";

            processingStatus.innerText =
                error.message
                || "AI processing failed.";
        } finally {

            processButton.disabled = false;
            processButton.innerText = "Process Video";
        }
    }

    // RESET WORKFLOW
    function resetWorkflow() {

        audioStatus.innerText =
            "Waiting...";

        transcriptionStatus.innerText =
            "Waiting...";

        extractionStatus.innerText =
            "Waiting...";

        transcript.value = "";

        jsonResult.textContent = "";

    }

    // RESET PAGE
    function resetPage() {

        videoList.innerHTML = "";

        videoResultsSection.classList.add(
            "d-none"
        );

        processingSection.classList.add(
            "d-none"
        );

        transcriptSection.classList.add(
            "d-none"
        );

        resultSection.classList.add(
            "d-none"
        );

        searchStatus.classList.add(
            "d-none"
        );

    }

    // SEARCH STATUS
    function showSearchStatus(
        message,
        type
    ) {

        searchStatus.className =
            "alert alert-" + type;

        searchStatus.innerText =
            message;

        searchStatus.classList.remove(
            "d-none"
        );
    }

    // ERROR HANDLING
    async function getErrorMessage(response) {

        try {

            const data =
                await response.json();


            return data.message
                || data.error
                || "Server error: "
                + response.status;

        } catch (error) {

            return "Server error: "
                + response.status;
        }
    }

    // COPY JSON
    copyJsonButton.addEventListener(
        "click",
        async function () {

            const json =
                jsonResult.textContent;


            if (!json) {
                return;
            }


            try {

                await navigator.clipboard.writeText(
                    json
                );


                copyJsonButton.innerText =
                    "Copied!";


                setTimeout(
                    function () {

                        copyJsonButton.innerText =
                            "Copy JSON";

                    },
                    1500
                );

            } catch (error) {

                console.error(
                    "Unable to copy JSON:",
                    error
                );
            }

        }
    );

});
