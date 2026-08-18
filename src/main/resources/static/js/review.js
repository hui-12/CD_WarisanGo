/* =========================================================
   WarisanGo - Review & Rating Module
   Review-specific page behavior and local photo upload UI.
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    initializeDeleteModal();
    initializeLikeButtons();
    initializeCharacterCounters();
    initializePhotoUpload();
    initializeCommentPreview();
    protectOwnerActions();
});

function initializeDeleteModal() {
    const modal = document.getElementById("reviewDeleteModal");
    const form = document.getElementById("reviewDeleteForm");

    if (!modal || !form) {
        return;
    }

    document.querySelectorAll("[data-review-delete]").forEach(function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();

            const reviewId = button.getAttribute("data-review-id");
            if (!reviewId) {
                return;
            }

            form.action = "/reviews/delete/" + encodeURIComponent(reviewId);
            modal.classList.add("open");
            modal.setAttribute("aria-hidden", "false");
            document.body.style.overflow = "hidden";
        });
    });

    document.querySelectorAll("[data-cancel-delete]").forEach(function (button) {
        button.addEventListener("click", closeModal);
    });

    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && modal.classList.contains("open")) {
            closeModal();
        }
    });

    function closeModal() {
        modal.classList.remove("open");
        modal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    }
}

function initializeLikeButtons() {
    document.querySelectorAll("[data-review-like], [data-comment-like]").forEach(function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();

            const likeUrl = button.dataset.likeUrl;
            if (!likeUrl || button.disabled) {
                return;
            }

            button.disabled = true;

            fetch(likeUrl, {
                method: "POST",
                headers: {
                    "Accept": "application/json",
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("Like request failed with status " + response.status);
                    }

                    return response.json();
                })
                .then(function (result) {
                    updateLikeButton(button, result);
                })
                .catch(function (error) {
                    console.error("Could not update like:", error);
                })
                .finally(function () {
                    button.disabled = false;
                });
        });
    });
}

function updateLikeButton(button, result) {
    const liked = result.liked === true;
    const icon = button.querySelector("[data-like-icon]");
    const count = button.querySelector("[data-like-count]");

    button.dataset.liked = String(liked);
    button.classList.toggle("liked", liked);
    button.setAttribute("aria-pressed", String(liked));

    if (icon) {
        icon.textContent = liked ? "♥" : "♡";
    }

    if (count) {
        count.textContent = result.likeCount;
    }
}

function initializeCharacterCounters() {
    document.querySelectorAll("textarea[data-review-counter]").forEach(function (textarea) {
        const counter = document.getElementById(textarea.getAttribute("data-review-counter"));

        if (!counter) {
            return;
        }

        const updateCounter = function () {
            counter.textContent = textarea.value.length;
        };

        textarea.addEventListener("input", updateCounter);
        updateCounter();
    });
}

function initializePhotoUpload() {
    const uploadArea = document.getElementById("reviewUploadArea");
    const fileInput = document.getElementById("reviewPhotos");
    const previewContainer = document.getElementById("reviewUploadPreview");

    if (!uploadArea || !fileInput || !previewContainer) {
        return;
    }

    const MAX_PHOTOS = 10;
    const allowedTypes = new Set(["image/jpeg", "image/png", "image/webp"]);
    let selectedFiles = [];

    uploadArea.addEventListener("click", function (event) {
        if (event.target !== fileInput) {
            fileInput.click();
        }
    });

    uploadArea.addEventListener("keydown", function (event) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            fileInput.click();
        }
    });

    fileInput.addEventListener("change", function () {
        addFiles(Array.from(fileInput.files || []));
        syncInputFiles();
    });

    const form = uploadArea.closest("form");
    if (form) {
        form.addEventListener("submit", function () {
            // Re-attach the selected File objects immediately before multipart submit.
            syncInputFiles();
        });
    }

    uploadArea.addEventListener("dragover", function (event) {
        event.preventDefault();
        uploadArea.classList.add("drag-over");
    });

    uploadArea.addEventListener("dragleave", function () {
        uploadArea.classList.remove("drag-over");
    });

    uploadArea.addEventListener("drop", function (event) {
        event.preventDefault();
        uploadArea.classList.remove("drag-over");
        addFiles(Array.from(event.dataTransfer.files || []));
        syncInputFiles();
    });

    document.querySelectorAll("input[name='removePhotoIds']").forEach(function (checkbox) {
        checkbox.addEventListener("change", function () {
            trimFilesToAvailableSlots();
            renderPreview();
            syncInputFiles();
        });
    });

    function addFiles(files) {
        const availableSlots = getAvailableSlots();

        for (const file of files) {
            if (!allowedTypes.has(file.type)) {
                showPhotoError("Only JPG, PNG, and WebP images are allowed.");
                continue;
            }

            if (file.size > 10 * 1024 * 1024) {
                showPhotoError("Each review photo must be 10 MB or smaller.");
                continue;
            }

            if (selectedFiles.length >= availableSlots) {
                showPhotoError("A review can contain a maximum of 10 photos.");
                break;
            }

            const duplicate = selectedFiles.some(function (existingFile) {
                return existingFile.name === file.name
                    && existingFile.size === file.size
                    && existingFile.lastModified === file.lastModified;
            });

            if (!duplicate) {
                selectedFiles.push(file);
            }
        }

        renderPreview();
        syncInputFiles();
    }

    function renderPreview() {
        previewContainer.innerHTML = "";

        selectedFiles.forEach(function (file, index) {
            const wrapper = document.createElement("div");
            wrapper.className = "review-upload-preview-item";

            const image = document.createElement("img");
            image.alt = "Selected review photo";
            image.src = URL.createObjectURL(file);

            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.className = "review-upload-remove";
            removeButton.textContent = "×";
            removeButton.setAttribute("aria-label", "Remove photo");
            removeButton.addEventListener("click", function (event) {
                event.preventDefault();
                event.stopPropagation();
                selectedFiles.splice(index, 1);
                renderPreview();
                syncInputFiles();
            });

            wrapper.appendChild(image);
            wrapper.appendChild(removeButton);
            previewContainer.appendChild(wrapper);
        });
    }

    function getAvailableSlots() {
        const existingCount = Number(uploadArea.dataset.existingPhotoCount || 0);
        const removedCount = document.querySelectorAll("input[name='removePhotoIds']:checked").length;
        return Math.max(0, MAX_PHOTOS - existingCount + removedCount);
    }

    function trimFilesToAvailableSlots() {
        const availableSlots = getAvailableSlots();
        if (selectedFiles.length > availableSlots) {
            selectedFiles = selectedFiles.slice(0, availableSlots);
            showPhotoError("Some new photos were removed because the review limit is 10 photos.");
        }
    }

    function syncInputFiles() {
        if (typeof DataTransfer === "undefined") {
            return false;
        }

        try {
            const dataTransfer = new DataTransfer();
            selectedFiles.forEach(function (file) {
                dataTransfer.items.add(file);
            });
            fileInput.files = dataTransfer.files;
            return true;
        } catch (error) {
            return false;
        }
    }

    function showPhotoError(message) {
        let error = document.getElementById("reviewPhotoError");

        if (!error) {
            error = document.createElement("span");
            error.id = "reviewPhotoError";
            error.className = "review-field-error";
            uploadArea.insertAdjacentElement("afterend", error);
        }

        error.textContent = message;
        error.hidden = false;
    }
}

function initializeCommentPreview() {
    document.querySelectorAll(".review-comment-preview details").forEach(function (details) {
        details.addEventListener("toggle", function () {
            const label = details.querySelector("summary span:last-child");

            if (label) {
                label.textContent = details.open ? "Collapse" : "Expand";
            }
        });
    });
}

function protectOwnerActions() {
    document.querySelectorAll(".review-owner-actions").forEach(function (actions) {
        actions.addEventListener("click", function (event) {
            event.stopPropagation();
        });
    });
}
