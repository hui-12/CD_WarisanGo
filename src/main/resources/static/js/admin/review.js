/* =========================================================
   WarisanGo - Review & Rating Module
   All Review + Comment page JavaScript
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       1. DELETE REVIEW MODAL
       ===================================================== */

    const deleteModal =
        document.getElementById("reviewDeleteModal");

    const deleteForm =
        document.getElementById("reviewDeleteForm");


    function openDeleteModal(reviewId) {

        if (!deleteModal || !deleteForm || !reviewId) {
            return;
        }

        deleteForm.action =
            "/reviews/delete/" + encodeURIComponent(reviewId);

        deleteModal.classList.add("open");

        deleteModal.setAttribute(
            "aria-hidden",
            "false"
        );

        document.body.style.overflow = "hidden";
    }


    function closeDeleteModal() {

        if (!deleteModal) {
            return;
        }

        deleteModal.classList.remove("open");

        deleteModal.setAttribute(
            "aria-hidden",
            "true"
        );

        document.body.style.overflow = "";
    }


    document
        .querySelectorAll("[data-review-delete]")
        .forEach(function (button) {

            button.addEventListener(
                "click",
                function (event) {

                    event.preventDefault();
                    event.stopPropagation();

                    const reviewId =
                        button.getAttribute(
                            "data-review-id"
                        );

                    openDeleteModal(reviewId);
                }
            );

        });


    document
        .querySelectorAll("[data-cancel-delete]")
        .forEach(function (button) {

            button.addEventListener(
                "click",
                closeDeleteModal
            );

        });


    if (deleteModal) {

        deleteModal.addEventListener(
            "click",
            function (event) {

                if (event.target === deleteModal) {
                    closeDeleteModal();
                }

            }
        );

    }


    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape" &&
                deleteModal &&
                deleteModal.classList.contains("open")
            ) {
                closeDeleteModal();
            }

        }
    );


    /* =====================================================
       2. REVIEW LIKE
       ===================================================== */

    document
        .querySelectorAll("[data-review-like]")
        .forEach(function (button) {

            button.addEventListener(
                "click",
                function (event) {

                    event.preventDefault();
                    event.stopPropagation();

                    const liked =
                        button.dataset.liked === "true";

                    button.dataset.liked =
                        liked ? "false" : "true";

                    button.classList.toggle(
                        "liked",
                        !liked
                    );

                }
            );

        });


    /* =====================================================
       3. COMMENT LIKE
       ===================================================== */

    document
        .querySelectorAll("[data-comment-like]")
        .forEach(function (button) {

            button.addEventListener(
                "click",
                function (event) {

                    event.preventDefault();
                    event.stopPropagation();

                    const liked =
                        button.dataset.liked === "true";

                    button.dataset.liked =
                        liked ? "false" : "true";

                    button.classList.toggle(
                        "liked",
                        !liked
                    );

                }
            );

        });


    /* =====================================================
       4. REVIEW TEXT CHARACTER COUNTER
       ===================================================== */

    document
        .querySelectorAll(
            "textarea[data-review-counter]"
        )
        .forEach(function (textarea) {

            const counterId =
                textarea.getAttribute(
                    "data-review-counter"
                );

            const counter =
                document.getElementById(counterId);


            function updateCounter() {

                if (!counter) {
                    return;
                }

                counter.textContent =
                    textarea.value.length;
            }


            textarea.addEventListener(
                "input",
                updateCounter
            );

            updateCounter();

        });


    /* =====================================================
       5. DRAG & DROP PHOTO UPLOAD
       ===================================================== */

    const uploadArea =
        document.getElementById("reviewUploadArea");

    const fileInput =
        document.getElementById("reviewPhotos");

    const previewContainer =
        document.getElementById(
            "reviewUploadPreview"
        );


    if (
        uploadArea &&
        fileInput &&
        previewContainer
    ) {

        const MAX_PHOTOS = 10;

        let selectedFiles = [];


        /* -------------------------------------------------
           Open file picker
           ------------------------------------------------- */

        uploadArea.addEventListener(
            "click",
            function () {

                fileInput.click();

            }
        );


        /* -------------------------------------------------
           Keyboard accessibility
           ------------------------------------------------- */

        uploadArea.addEventListener(
            "keydown",
            function (event) {

                if (
                    event.key === "Enter" ||
                    event.key === " "
                ) {

                    event.preventDefault();

                    fileInput.click();
                }

            }
        );


        /* -------------------------------------------------
           File picker
           ------------------------------------------------- */

        fileInput.addEventListener(
            "change",
            function () {

                const files =
                    Array.from(
                        fileInput.files || []
                    );

                addFiles(files);

                /*
                 * Allows selecting the same file again.
                 */
                fileInput.value = "";

            }
        );


        /* -------------------------------------------------
           Drag over upload area
           ------------------------------------------------- */

        uploadArea.addEventListener(
            "dragover",
            function (event) {

                event.preventDefault();

                uploadArea.classList.add(
                    "drag-over"
                );

            }
        );


        /* -------------------------------------------------
           Drag leave
           ------------------------------------------------- */

        uploadArea.addEventListener(
            "dragleave",
            function () {

                uploadArea.classList.remove(
                    "drag-over"
                );

            }
        );


        /* -------------------------------------------------
           Drop files
           ------------------------------------------------- */

        uploadArea.addEventListener(
            "drop",
            function (event) {

                event.preventDefault();

                uploadArea.classList.remove(
                    "drag-over"
                );

                const files =
                    Array.from(
                        event.dataTransfer.files || []
                    );

                addFiles(files);

            }
        );


        /* -------------------------------------------------
           Add files
           ------------------------------------------------- */

        function addFiles(files) {

            files.forEach(
                function (file) {

                    /*
                     * Only image files.
                     */
                    if (
                        !file.type.startsWith(
                            "image/"
                        )
                    ) {
                        return;
                    }


                    /*
                     * Maximum 10 photos.
                     */
                    if (
                        selectedFiles.length >=
                        MAX_PHOTOS
                    ) {
                        return;
                    }


                    /*
                     * Prevent duplicate file.
                     */
                    const duplicate =
                        selectedFiles.some(
                            function (existingFile) {

                                return (
                                    existingFile.name ===
                                    file.name &&

                                    existingFile.size ===
                                    file.size &&

                                    existingFile.lastModified ===
                                    file.lastModified
                                );

                            }
                        );


                    if (duplicate) {
                        return;
                    }


                    selectedFiles.push(file);

                }
            );


            renderPreview();

        }


        /* -------------------------------------------------
           Render preview
           ------------------------------------------------- */

        function renderPreview() {

            previewContainer.innerHTML = "";


            selectedFiles.forEach(
                function (file, index) {

                    const wrapper =
                        document.createElement(
                            "div"
                        );

                    wrapper.className =
                        "review-upload-preview-item";


                    const image =
                        document.createElement(
                            "img"
                        );

                    image.alt =
                        "Selected review photo";


                    const removeButton =
                        document.createElement(
                            "button"
                        );

                    removeButton.type =
                        "button";

                    removeButton.className =
                        "review-upload-remove";

                    removeButton.textContent =
                        "×";

                    removeButton.setAttribute(
                        "aria-label",
                        "Remove photo"
                    );


                    removeButton.addEventListener(
                        "click",
                        function (event) {

                            event.preventDefault();
                            event.stopPropagation();

                            selectedFiles.splice(
                                index,
                                1
                            );

                            renderPreview();

                        }
                    );


                    const reader =
                        new FileReader();


                    reader.onload =
                        function (event) {

                            image.src =
                                event.target.result;

                        };


                    reader.readAsDataURL(
                        file
                    );


                    wrapper.appendChild(
                        image
                    );

                    wrapper.appendChild(
                        removeButton
                    );

                    previewContainer.appendChild(
                        wrapper
                    );

                }
            );

        }

    }


    /* =====================================================
       6. COMMENT PREVIEW
       ===================================================== */

    document
        .querySelectorAll(
            ".review-comment-preview details"
        )
        .forEach(function (details) {

            details.addEventListener(
                "toggle",
                function () {

                    const label =
                        details.querySelector(
                            "summary span:last-child"
                        );

                    if (!label) {
                        return;
                    }

                    label.textContent =
                        details.open
                            ? "Collapse"
                            : "Expand";

                }
            );

        });


    /* =====================================================
       7. PROTECT OWNER ACTIONS
       ===================================================== */

    document
        .querySelectorAll(
            ".review-owner-actions"
        )
        .forEach(function (actions) {

            actions.addEventListener(
                "click",
                function (event) {

                    event.stopPropagation();

                }
            );

        });


});