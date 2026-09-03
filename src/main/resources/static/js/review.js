/* =========================================================
   WarisanGo - Review & Rating Module
   Review-specific page behavior and local photo upload UI.
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {
  initializeDeleteModal();
  initializeReportModal();
  initializeLikeButtons();
  initializeCharacterCounters();
  initializeModerationForms();
  initializeModerationDeleteConfirmation();
  initializeReportTable();
  initializePhotoUpload();
  initializeReviewPhotoViewer();
  initializeCommentPreview();
  initializeCommentReplies();
  protectOwnerActions();
});

function initializeDeleteModal() {
  const modal = document.getElementById('reviewDeleteModal');
  const form = document.getElementById('reviewDeleteForm');

  if (!modal || !form) {
    return;
  }

  document.querySelectorAll('[data-review-delete]').forEach(function (button) {
    button.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();

      const reviewId = button.getAttribute('data-review-id');
      if (!reviewId) {
        return;
      }

      form.action = '/reviews/delete/' + encodeURIComponent(reviewId);
      modal.classList.add('open');
      modal.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
    });
  });

  document.querySelectorAll('[data-cancel-delete]').forEach(function (button) {
    button.addEventListener('click', closeModal);
  });

  modal.addEventListener('click', function (event) {
    if (event.target === modal) {
      closeModal();
    }
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && modal.classList.contains('open')) {
      closeModal();
    }
  });

  function closeModal() {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }
}

function initializeReportModal() {
  const modal = document.getElementById('reviewReportModal');
  const form = modal ? modal.querySelector('[data-report-form]') : null;

  if (!modal || !form) {
    return;
  }

  const error = form.querySelector('[data-report-error]');

  document.querySelectorAll('[data-report-target]').forEach(function (button) {
    button.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();

      form.elements.targetType.value = button.dataset.reportTarget || '';
      form.elements.reviewId.value = button.dataset.reviewId || '';
      form.elements.commentId.value = button.dataset.commentId || '';
      form.elements.reason.value = '';
      if (error) {
        error.hidden = true;
        error.textContent = '';
      }

      modal.classList.add('open');
      modal.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
    });
  });

  document.querySelectorAll('[data-cancel-report]').forEach(function (button) {
    button.addEventListener('click', closeModal);
  });

  modal.addEventListener('click', function (event) {
    if (event.target === modal) {
      closeModal();
    }
  });

  form.addEventListener('submit', function (event) {
    event.preventDefault();

    const submitButton = form.querySelector("button[type='submit']");
    if (submitButton) {
      submitButton.disabled = true;
    }

    fetch(form.action, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
      },
      body: new FormData(form),
    })
      .then(function (response) {
        return response.json().then(function (result) {
          if (!response.ok) {
            throw new Error(result.message || 'The report could not be submitted.');
          }
          return result;
        });
      })
      .then(function (result) {
        closeModal();
        window.alert(result.message || 'Thank you. Your report has been submitted.');
      })
      .catch(function (reportError) {
        if (error) {
          error.textContent = reportError.message;
          error.hidden = false;
        }
      })
      .finally(function () {
        if (submitButton) {
          submitButton.disabled = false;
        }
      });
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && modal.classList.contains('open')) {
      closeModal();
    }
  });

  function closeModal() {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }
}

function initializeModerationForms() {
  document.querySelectorAll('form[data-content-form]').forEach(function (form) {
    const textarea = form.querySelector('[data-moderated-text]');

    if (!textarea) {
      return;
    }

    const error = document.createElement('span');
    error.className = 'review-field-error review-moderation-error';
    error.hidden = true;
    textarea.insertAdjacentElement('afterend', error);

    form.addEventListener('submit', function (event) {
      if (form.dataset.moderationAllowed === 'true') {
        delete form.dataset.moderationAllowed;
        return;
      }

      if (!textarea.value.trim()) {
        return;
      }

      event.preventDefault();
      error.hidden = true;

      fetch('/reviews/moderation/check', {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded',
          'X-Requested-With': 'XMLHttpRequest',
        },
        body: 'text=' + encodeURIComponent(textarea.value),
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('The content check could not be completed.');
          }
          return response.json();
        })
        .then(function (result) {
          if (!result.allowed) {
            error.textContent = result.message || 'Please remove prohibited language before submitting.';
            error.hidden = false;
            textarea.focus();
            return;
          }

          form.dataset.moderationAllowed = 'true';
          if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
          } else {
            form.submit();
          }
        })
        .catch(function (moderationError) {
          error.textContent = moderationError.message;
          error.hidden = false;
        });
    });
  });
}

function initializeModerationDeleteConfirmation() {
  document.querySelectorAll('form[data-moderation-delete]').forEach(function (form) {
    form.addEventListener('submit', function (event) {
      if (!window.confirm('Delete this content permanently? This action cannot be undone.')) {
        event.preventDefault();
      }
    });
  });
}

function initializeReportTable() {
  const table = document.querySelector('[data-report-table]');
  if (!table) {
    return;
  }

  const tableBody = table.querySelector('tbody');
  const searchInput = document.querySelector('[data-report-search]');
  const categorySelect = document.querySelector('[data-report-category]');
  const sortButtons = document.querySelectorAll('[data-report-sort-key]');
  const sortIndicators = document.querySelectorAll('[data-report-sort-indicator]');
  const noResultsRow = table.querySelector('[data-report-no-results]');
  const reportRows = Array.from(table.querySelectorAll('.review-admin-table-row'));
  const actionsModal = document.querySelector('[data-report-actions-modal]');
  const actionsModalBody = actionsModal ? actionsModal.querySelector('[data-report-actions-modal-body]') : null;
  const actionsModalMessage = actionsModal ? actionsModal.querySelector('[data-report-actions-message]') : null;
  let activeActions = null;
  let activePlaceholder = null;
  let activeButton = null;
  let sortKey = 'default';
  let sortDirection = 'desc';

  document.querySelectorAll('[data-report-actions-toggle]').forEach(function (button) {
    button.addEventListener('click', function () {
      const row = button.closest('tr');
      const actions = row ? row.querySelector('[data-report-actions]') : null;
      const placeholder = row ? row.querySelector('[data-report-actions-placeholder]') : null;

      if (!actions || !placeholder || !actionsModal || !actionsModalBody) {
        return;
      }

      closeActionsModal();

      const username = row.querySelector('.review-admin-table-user strong');
      const message = row.querySelector('.review-admin-table-message span');
      const reportType = row.dataset.reportType || 'REPORT';

      if (actionsModalMessage) {
        actionsModalMessage.textContent =
          reportType + ' report from ' + (username ? username.textContent.trim() : 'this user') + '. Choose an action.';
      }

      actionsModalBody.appendChild(actions);
      actions.hidden = false;
      activeActions = actions;
      activePlaceholder = placeholder;
      activeButton = button;
      button.setAttribute('aria-expanded', 'true');
      actionsModal.hidden = false;
      actionsModal.classList.add('open');
      document.body.style.overflow = 'hidden';

      if (message) {
        message.setAttribute('aria-current', 'true');
      }
    });
  });

  function sortReports() {
    reportRows.sort(function (firstRow, secondRow) {
      if (sortKey === 'default') {
        const firstPending = firstRow.dataset.reportStatus === 'PENDING' ? 0 : 1;
        const secondPending = secondRow.dataset.reportStatus === 'PENDING' ? 0 : 1;

        if (firstPending !== secondPending) {
          return firstPending - secondPending;
        }

        return (secondRow.dataset.reportCreated || '').localeCompare(firstRow.dataset.reportCreated || '');
      }

      const firstValue = getSortValue(firstRow, sortKey);
      const secondValue = getSortValue(secondRow, sortKey);
      const comparison = firstValue.localeCompare(secondValue, undefined, { numeric: true, sensitivity: 'base' });

      return sortDirection === 'asc' ? comparison : -comparison;
    });

    reportRows.forEach(function (row) {
      tableBody.insertBefore(row, noResultsRow);
    });

    updateSortIndicator();
  }

  function getSortValue(row, key) {
    if (key === 'date') {
      return row.dataset.reportCreated || '';
    }
    if (key === 'status') {
      return row.dataset.reportStatus || '';
    }

    const selector = key === 'username' ? '.review-admin-table-user strong' : '.review-admin-table-message span';
    const field = row.querySelector(selector);
    return field ? field.textContent.trim() : '';
  }

  function filterReports() {
    const searchTerm = searchInput ? searchInput.value.trim().toLowerCase() : '';
    const selectedCategory = categorySelect ? categorySelect.value : 'ALL';
    let visibleCount = 0;

    reportRows.forEach(function (row) {
      const rowText = Array.from(row.querySelectorAll('[data-report-search-field]'))
        .map(function (field) {
          return field.textContent;
        })
        .join(' ')
        .toLowerCase();
      const categoryMatches = selectedCategory === 'ALL' || row.dataset.reportType === selectedCategory;
      const searchMatches = !searchTerm || rowText.includes(searchTerm);
      const visible = categoryMatches && searchMatches;

      row.hidden = !visible;
      if (visible) {
        visibleCount += 1;
      }
    });

    if (noResultsRow) {
      noResultsRow.hidden = visibleCount !== 0;
    }
  }

  function updateSearchPlaceholder() {
    if (!searchInput) {
      return;
    }

    if (categorySelect && categorySelect.value === 'REVIEW') {
      searchInput.placeholder = 'Search username or review message';
    } else if (categorySelect && categorySelect.value === 'COMMENT') {
      searchInput.placeholder = 'Search username or comment message';
    } else {
      searchInput.placeholder = 'Search username or review/comment message';
    }
  }

  function updateSortIndicator() {
    sortIndicators.forEach(function (indicator) {
      const indicatorKey = indicator.dataset.reportSortIndicator;

      if (sortKey === 'default') {
        indicator.textContent = indicatorKey === 'date' ? '↓' : '↕';
        return;
      }

      if (indicatorKey !== sortKey) {
        indicator.textContent = '↕';
        return;
      }

      indicator.textContent = sortDirection === 'asc' ? '↑' : '↓';
    });
  }

  function closeActionsModal() {
    if (activeActions && activePlaceholder) {
      activePlaceholder.insertAdjacentElement('afterend', activeActions);
      activeActions.hidden = true;
    }

    if (activeButton) {
      activeButton.setAttribute('aria-expanded', 'false');
    }

    activeActions = null;
    activePlaceholder = null;
    activeButton = null;

    if (actionsModal) {
      actionsModal.hidden = true;
      actionsModal.classList.remove('open');
    }
    document.body.style.overflow = '';
  }

  sortReports();
  updateSearchPlaceholder();
  filterReports();

  if (searchInput) {
    searchInput.addEventListener('input', filterReports);
  }
  if (categorySelect) {
    categorySelect.addEventListener('change', function () {
      updateSearchPlaceholder();
      filterReports();
    });
  }
  sortButtons.forEach(function (sortButton) {
    sortButton.addEventListener('click', function () {
      const requestedKey = sortButton.dataset.reportSortKey;

      if (sortKey === requestedKey) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        sortKey = requestedKey;
        sortDirection = requestedKey === 'date' ? 'desc' : 'asc';
      }

      sortReports();
    });
  });
  if (actionsModal) {
    actionsModal.addEventListener('click', function (event) {
      if (event.target === actionsModal) {
        closeActionsModal();
      }
    });

    const closeButton = actionsModal.querySelector('[data-report-actions-close]');
    if (closeButton) {
      closeButton.addEventListener('click', closeActionsModal);
    }

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && !actionsModal.hidden) {
        closeActionsModal();
      }
    });
  }
}

function initializeLikeButtons() {
  document.querySelectorAll('[data-review-like], [data-comment-like]').forEach(function (button) {
    button.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();

      const likeUrl = button.dataset.likeUrl;
      if (!likeUrl || button.disabled) {
        return;
      }

      button.disabled = true;

      fetch(likeUrl, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
        },
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('Like request failed with status ' + response.status);
          }

          return response.json();
        })
        .then(function (result) {
          updateLikeButton(button, result);
        })
        .catch(function (error) {
          console.error('Could not update like:', error);
        })
        .finally(function () {
          button.disabled = false;
        });
    });
  });
}

function updateLikeButton(button, result) {
  const liked = result.liked === true;
  const icon = button.querySelector('[data-like-icon]');
  const count = button.querySelector('[data-like-count]');

  button.dataset.liked = String(liked);
  button.classList.toggle('liked', liked);
  button.setAttribute('aria-pressed', String(liked));

  if (icon) {
    icon.textContent = liked ? '♥' : '♡';
  }

  if (count) {
    count.textContent = result.likeCount;
  }
}

function initializeCharacterCounters() {
  document.querySelectorAll('textarea[data-review-counter]').forEach(function (textarea) {
    const counter = document.getElementById(textarea.getAttribute('data-review-counter'));

    if (!counter) {
      return;
    }

    const updateCounter = function () {
      counter.textContent = textarea.value.length;
    };

    textarea.addEventListener('input', updateCounter);
    updateCounter();
  });
}

function initializePhotoUpload() {
  const uploadArea = document.getElementById('reviewUploadArea');
  const fileInput = document.getElementById('reviewPhotos');
  const previewContainer = document.getElementById('reviewUploadPreview');

  if (!uploadArea || !fileInput || !previewContainer) {
    return;
  }

  const MAX_PHOTOS = 10;
  const allowedTypes = new Set(['image/jpeg', 'image/png', 'image/webp']);
  let selectedFiles = [];

  uploadArea.addEventListener('click', function (event) {
    if (event.target !== fileInput) {
      fileInput.click();
    }
  });

  uploadArea.addEventListener('keydown', function (event) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      fileInput.click();
    }
  });

  fileInput.addEventListener('change', function () {
    addFiles(Array.from(fileInput.files || []));
    syncInputFiles();
  });

  const form = uploadArea.closest('form');
  if (form) {
    form.addEventListener('submit', function () {
      // Re-attach the selected File objects immediately before multipart submit.
      syncInputFiles();
    });
  }

  uploadArea.addEventListener('dragover', function (event) {
    event.preventDefault();
    uploadArea.classList.add('drag-over');
  });

  uploadArea.addEventListener('dragleave', function () {
    uploadArea.classList.remove('drag-over');
  });

  uploadArea.addEventListener('drop', function (event) {
    event.preventDefault();
    uploadArea.classList.remove('drag-over');
    addFiles(Array.from(event.dataTransfer.files || []));
    syncInputFiles();
  });

  document.querySelectorAll("input[name='removePhotoIds']").forEach(function (checkbox) {
    checkbox.addEventListener('change', function () {
      trimFilesToAvailableSlots();
      renderPreview();
      syncInputFiles();
    });
  });

  function addFiles(files) {
    const availableSlots = getAvailableSlots();

    for (const file of files) {
      if (!allowedTypes.has(file.type)) {
        showPhotoError('Only JPG, PNG, and WebP images are allowed.');
        continue;
      }

      if (file.size > 10 * 1024 * 1024) {
        showPhotoError('Each review photo must be 10 MB or smaller.');
        continue;
      }

      if (selectedFiles.length >= availableSlots) {
        showPhotoError('A review can contain a maximum of 10 photos.');
        break;
      }

      const duplicate = selectedFiles.some(function (existingFile) {
        return (
          existingFile.name === file.name &&
          existingFile.size === file.size &&
          existingFile.lastModified === file.lastModified
        );
      });

      if (!duplicate) {
        selectedFiles.push(file);
      }
    }

    renderPreview();
    syncInputFiles();
  }

  function renderPreview() {
    previewContainer.innerHTML = '';

    selectedFiles.forEach(function (file, index) {
      const wrapper = document.createElement('div');
      wrapper.className = 'review-upload-preview-item';

      const image = document.createElement('img');
      image.alt = 'Selected review photo';
      image.src = URL.createObjectURL(file);

      const removeButton = document.createElement('button');
      removeButton.type = 'button';
      removeButton.className = 'review-upload-remove';
      removeButton.textContent = '×';
      removeButton.setAttribute('aria-label', 'Remove photo');
      removeButton.addEventListener('click', function (event) {
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
      showPhotoError('Some new photos were removed because the review limit is 10 photos.');
    }
  }

  function syncInputFiles() {
    if (typeof DataTransfer === 'undefined') {
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
    let error = document.getElementById('reviewPhotoError');

    if (!error) {
      error = document.createElement('span');
      error.id = 'reviewPhotoError';
      error.className = 'review-field-error';
      uploadArea.insertAdjacentElement('afterend', error);
    }

    error.textContent = message;
    error.hidden = false;
  }
}

function initializeReviewPhotoViewer() {
  const viewer = document.getElementById('reviewPhotoViewer');
  const viewerImage = viewer ? viewer.querySelector('[data-review-photo-viewer-image]') : null;

  if (!viewer || !viewerImage) {
    return;
  }

  const closeViewer = function () {
    viewer.hidden = true;
    viewer.setAttribute('aria-hidden', 'true');
    viewerImage.removeAttribute('src');
    document.body.classList.remove('review-photo-viewer-open');
  };

  const openViewer = function (photo) {
    const source = photo.dataset.photoSrc || photo.currentSrc || photo.src;

    if (!source) {
      return;
    }

    viewerImage.src = source;
    viewer.hidden = false;
    viewer.setAttribute('aria-hidden', 'false');
    document.body.classList.add('review-photo-viewer-open');
  };

  document.querySelectorAll('.review-photo-open').forEach(function (photo) {
    photo.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();
      openViewer(photo);
    });

    photo.addEventListener('keydown', function (event) {
      if (event.key !== 'Enter' && event.key !== ' ') {
        return;
      }

      event.preventDefault();
      event.stopPropagation();
      openViewer(photo);
    });
  });

  document.querySelectorAll('[data-close-photo-viewer]').forEach(function (button) {
    button.addEventListener('click', closeViewer);
  });

  viewer.addEventListener('click', function (event) {
    if (event.target === viewer) {
      closeViewer();
    }
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && !viewer.hidden) {
      closeViewer();
    }
  });
}

function initializeCommentPreview() {
  document.querySelectorAll('.review-comment-preview details').forEach(function (details) {
    details.addEventListener('toggle', function () {
      const label = details.querySelector('summary span:last-child');

      if (label) {
        label.textContent = details.open ? 'Collapse' : 'Expand';
      }
    });
  });
}

function initializeCommentReplies() {
  const form = document.querySelector('form[data-comment-form]');
  const replyIdInput = form ? form.querySelector("[name='replyToCommentId']") : null;
  const replyTarget = document.getElementById('replyTarget');
  const replyName = replyTarget ? replyTarget.querySelector('[data-reply-name]') : null;
  const commentText = form ? form.querySelector('[data-moderated-text]') : null;

  if (!form || !replyIdInput || !replyTarget || !replyName) {
    return;
  }

  document.querySelectorAll('[data-reply-trigger]').forEach(function (button) {
    button.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();

      replyIdInput.value = button.dataset.replyCommentId || '';
      replyName.textContent = button.dataset.replyTouristName || 'Visitor';
      replyTarget.hidden = false;

      if (commentText) {
        commentText.focus();
      }
    });
  });

  document.querySelectorAll('[data-cancel-reply]').forEach(function (button) {
    button.addEventListener('click', function () {
      clearReplyTarget();
    });
  });

  function clearReplyTarget() {
    replyIdInput.value = '';
    replyName.textContent = '';
    replyTarget.hidden = true;
  }
}

function protectOwnerActions() {
  document.querySelectorAll('.review-owner-actions').forEach(function (actions) {
    actions.addEventListener('click', function (event) {
      event.stopPropagation();
    });
  });
}
document.querySelectorAll('[data-progress-percentage]').forEach((progressBar) => {
  const percentage = Number(progressBar.dataset.progressPercentage || 0);
  progressBar.style.width = `${Math.max(0, Math.min(100, percentage))}%`;
});
