const searchButton =
    document.getElementById("searchButton");

searchButton.addEventListener(
    "click",
    searchVideos
);

// Log messages to the logs div
function log(message) {
    const logs =
        document.getElementById("logs");

    const timestamp =
        new Date().toLocaleTimeString();

    logs.innerHTML +=
        `<br>[${timestamp}] ${message}`;

    logs.scrollTop =
        logs.scrollHeight;
}

// Search for videos based on the keyword
async function searchVideos() {

    const keyword =
        document.getElementById("keyword").value.trim();

    if (!keyword) {
        alert(
            "Please enter keyword"
        );
        return;
    }

    try {
        log(
            "Searching YouTube..."
        );

        const response =
            await fetch(
                `/api/videos/search?keyword=${encodeURIComponent(keyword)}`
            );

        if (!response.ok) {
            throw new Error(
                "Video search failed"
            );
        }

        const videos =
            await response.json();

        log(
            `Found ${videos.length} videos`
        );

        loadVideos(videos);

    } catch(error) {
        log(
            `Error: ${error.message}`
        );
    }
}

// Load videos into the table
function loadVideos(videos) {

    const table =
        document.getElementById("videoTable");

    table.innerHTML = "";

    videos.forEach(video => {

        table.innerHTML += `
        <tr>
            <td>
                <img 
                    src="${video.thumbnail}"
                    alt="Video thumbnail">
            </td>
            <td>
                ${video.title}
            </td>
            <td>
                ${video.channel}
            </td>
            <td>
                ${video.publishedAt}
            </td>
            <td>
                <button
                    class="btn btn-success btn-sm process-button"
                    data-video-id="${video.videoId}">
                    Process
                </button>
            </td>
        </tr>
        `;
    });

    registerProcessButtons();

}

// Register click event listeners for all process buttons
function registerProcessButtons() {

    const buttons =
        document.querySelectorAll(
            ".process-button"
        );

    buttons.forEach(button => {

        button.addEventListener(
            "click",
            () => {
                processVideo(
                    button.dataset.videoId
                );
            }
        );
    });
}

// Process a video by sending a request to the backend
async function processVideo(videoId) {

    try {
        log(
            `Processing video: ${videoId}`
        );
        const response =
            await fetch(
                `/api/discovery/process/${videoId}`
            );

        if (!response.ok) {
            throw new Error(
                "AI processing failed"
            );
        }

        const record =
            await response.json();

        log(
            "AI Extraction Completed"
        );

        addPending(record);

    } catch(error) {
        log(
            `Error: ${error.message}`
        );
    }
}

// Add a new record to the pending table
function addPending(record) {

    const table =
        document.getElementById("pendingTable");

    table.innerHTML += `

    <tr>
        <td>
            ${record.businessName}
        </td>
        <td>
            ${record.foodName}
        </td>
        <td>
            ${record.location}
        </td>
        <td class="status text-warning">
            Pending
        </td>
    </tr>
    `;
}