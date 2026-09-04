document.addEventListener('DOMContentLoaded', function () {
  const keywordInput = document.getElementById('keyword');

  const searchButton = document.getElementById('searchButton');

  const videoSource = document.getElementById('videoSource');
  const tikTokOptions = document.getElementById('tikTokOptions');
  const maximumVideos = document.getElementById('maximumVideos');
  const scrollCount = document.getElementById('scrollCount');
  const waitTime = document.getElementById('waitTime');
  const resultsHeading = document.getElementById('resultsHeading');

  const searchStatus = document.getElementById('search-status');

  const videoResultsSection = document.getElementById('videoResultsSection');

  const videoList = document.getElementById('videoList');

  const toggleVideoResults = document.getElementById('toggleVideoResults');

  const videoResultsContent = document.getElementById('videoResultsContent');

  const processingSection = document.getElementById('processingSection');

  const processingStatus = document.getElementById('processingStatus');

  const processingProgressBar = document.getElementById('processing-progress-bar');

  const activeJobStorageKey = 'warisango.activeDiscoveryJob';

  const selectedVideoTitle = document.getElementById('selectedVideoTitle');

  const selectedVideoUrl = document.getElementById('selected-video-url');

  const audioStatus = document.getElementById('audioStatus');

  const transcriptionStatus = document.getElementById('transcriptionStatus');

  const extractionStatus = document.getElementById('extractionStatus');

  const transcriptSection = document.getElementById('transcriptSection');

  const transcript = document.getElementById('transcript');

  const resultSection = document.getElementById('resultSection');

  const jsonResult = document.getElementById('jsonResult');

  const copyJsonButton = document.getElementById('copyJsonButton');

  // SEARCH
  searchButton.addEventListener('click', searchVideos);

  videoSource.addEventListener('change', function () {
    const isTikTok = videoSource.value === 'tiktok';
    tikTokOptions.classList.toggle('d-none', !isTikTok);
    resultsHeading.textContent = isTikTok ? 'TikTok Results' : 'YouTube Results';
  });

  keywordInput.addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
      event.preventDefault();

      searchVideos();
    }
  });

  async function searchVideos() {
    const keyword = keywordInput.value.trim();

    if (!keyword) {
      showSearchStatus('Please enter a search keyword.', 'danger');

      return;
    }

    resetPage();

    searchButton.disabled = true;

    searchButton.innerText = 'Searching...';

    const isTikTok = videoSource.value === 'tiktok';

    showSearchStatus(
      isTikTok
        ? 'Searching TikTok in a visible browser. Complete any verification there when prompted.'
        : 'Searching YouTube videos...',
      'info'
    );

    try {
      const response = await fetch(isTikTok ? '/api/discovery/search/tiktok' : '/api/discovery/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(
          isTikTok
            ? {
                keyword: keyword,
                maximumVideos: Number(maximumVideos.value),
                scrollCount: Number(scrollCount.value),
                waitTime: Number(waitTime.value),
              }
            : { keyword: keyword }
        ),
      });

      const contentType = response.headers.get('content-type') || '';

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      if (!contentType.includes('application/json')) {
        throw new Error(
          response.url.includes('/login')
            ? 'Your admin session has expired. Please log in again.'
            : 'TikTok search returned an unexpected response. Check the server deployment and logs.'
        );
      }

      const videos = await response.json();

      if (!Array.isArray(videos) || videos.length === 0) {
        showSearchStatus('No videos were found.', 'warning');

        return;
      }

      displayVideos(videos, isTikTok);

      showSearchStatus(videos.length + ' video(s) found. Click Process Video to continue.', 'success');
    } catch (error) {
      console.error('Video search failed:', error);

      showSearchStatus(error.message || 'Unable to search videos.', 'danger');
    } finally {
      searchButton.disabled = false;

      searchButton.innerText = 'Search Videos';
    }
  }

  // DISPLAY VIDEOS
  function displayVideos(videos, isTikTok) {
    videoList.innerHTML = '';

    videos.forEach(function (video) {
      const column = document.createElement('div');

      column.className = 'col-md-6';

      const card = document.createElement('div');

      card.className = 'card video-card';

      const cardBody = document.createElement('div');

      cardBody.className = 'video-card-body';

      const title = document.createElement('div');

      title.className = 'video-title';

      title.textContent = video.title || 'TikTok video';

      if (isTikTok) {
        if (video.thumbnail) {
          const thumbnail = document.createElement('img');
          thumbnail.className = 'video-thumbnail';
          thumbnail.src = video.thumbnail;
          thumbnail.alt = video.title || 'TikTok video cover';
          thumbnail.loading = 'lazy';
          card.appendChild(thumbnail);
        }

        const videoLink = document.createElement('a');
        videoLink.className = 'tiktok-video-link';
        videoLink.href = video.videoUrl;
        videoLink.target = '_blank';
        videoLink.rel = 'noopener noreferrer';
        videoLink.textContent = video.videoUrl;

        const openButton = document.createElement('a');
        openButton.className = 'btn btn-outline-primary mt-3';
        openButton.href = video.videoUrl;
        openButton.target = '_blank';
        openButton.rel = 'noopener noreferrer';
        openButton.textContent = 'Open Video';

        const processButton = document.createElement('button');
        processButton.type = 'button';
        processButton.className = 'btn btn-success mt-3 ms-2';
        processButton.innerText = 'Process Video';
        processButton.addEventListener('click', function () {
          processVideo(video, processButton);
        });

        cardBody.appendChild(title);
        cardBody.appendChild(videoLink);
        cardBody.appendChild(openButton);
        cardBody.appendChild(processButton);
        card.appendChild(cardBody);
        column.appendChild(card);
        videoList.appendChild(column);
        return;
      }

      const thumbnail = document.createElement('img');
      thumbnail.className = 'video-thumbnail';
      thumbnail.src = video.thumbnail;
      thumbnail.alt = video.title;

      const channel = document.createElement('div');

      channel.className = 'video-channel';

      channel.textContent = video.channel;

      const description = document.createElement('div');

      description.className = 'video-description';

      description.textContent = video.description;

      const processButton = document.createElement('button');

      processButton.type = 'button';

      processButton.className = 'btn btn-success mt-3';

      processButton.innerText = 'Process Video';

      processButton.addEventListener('click', function () {
        processVideo(video, processButton);
      });

      cardBody.appendChild(title);

      cardBody.appendChild(channel);

      cardBody.appendChild(description);

      cardBody.appendChild(processButton);

      card.appendChild(thumbnail);

      card.appendChild(cardBody);

      column.appendChild(card);

      videoList.appendChild(column);
    });

    videoResultsSection.classList.remove('d-none');

    setVideoResultsExpanded(true);
  }

  // PROCESS VIDEO
  async function processVideo(video, processButton) {
    const videoUrl = video.videoUrl || 'https://www.youtube.com/watch?v=' + video.videoId;

    processingSection.classList.remove('d-none');

    transcriptSection.classList.add('d-none');

    resultSection.classList.add('d-none');

    selectedVideoTitle.textContent = video.title;

    selectedVideoUrl.textContent = videoUrl;

    resetWorkflow();

    processingStatus.className = 'alert alert-info';

    processingStatus.innerText = 'Starting AI Heritage Discovery...';

    processButton.disabled = true;
    processButton.innerText = 'Processing...';

    try {
      // STEP 1 YouTube → VideoAudioService

      audioStatus.innerText = 'Server is downloading the selected video audio...';

      transcriptionStatus.innerText = 'Waiting for server-side transcription...';

      extractionStatus.innerText = 'Waiting for Gemini extraction and Firebase save...';

      // STEP 2 Audio → AssemblyAI
      processingStatus.innerText = 'Processing selected video on the server...';

      const jobId = await window.WarisanGoWorkflow.start(videoUrl);
      saveActiveJob(jobId, video.title, videoUrl);
      await pollProcessingJob(jobId);

      // STEP 3 Transcript → Gemini
      // =====================================
      // DISPLAY GEMINI RESULT
      // =====================================
    } catch (error) {
      console.error('AI processing failed:', error);

      processingStatus.className = 'alert alert-danger';

      processingStatus.innerText = error.message || 'AI processing failed.';
    } finally {
      processButton.disabled = false;
      processButton.innerText = 'Process Video';
    }
  }

  function saveActiveJob(jobId, videoTitle, videoUrl) {
    localStorage.setItem(
      activeJobStorageKey,
      JSON.stringify({
        jobId: jobId,
        videoTitle: videoTitle,
        videoUrl: videoUrl,
      })
    );
  }

  function getActiveJob() {
    try {
      return JSON.parse(localStorage.getItem(activeJobStorageKey));
    } catch (error) {
      localStorage.removeItem(activeJobStorageKey);
      return null;
    }
  }

  async function pollProcessingJob(jobId) {
    const maximumTransientFailures = 20;
    let transientFailures = 0;

    while (true) {
      let job;

      try {
        job = await window.WarisanGoWorkflow.get(jobId);
        transientFailures = 0;
      } catch (error) {
        const isGatewayFailure = [502, 503, 504].includes(error.status);
        const isNetworkFailure = typeof error.status === 'undefined';

        if (!isGatewayFailure && !isNetworkFailure) {
          throw error;
        }

        transientFailures++;
        processingStatus.className = 'alert alert-warning';
        processingStatus.innerText = 'Server is temporarily unavailable. Reconnecting...';

        if (transientFailures >= maximumTransientFailures) {
          throw new Error(
            'The processing server remained unavailable. Check the Render service logs and try again.'
          );
        }

        await waitBeforeNextStatusCheck(3000);
        continue;
      }

      updateProcessingDisplay(job);

      if (job.status === 'COMPLETED') {
        displayCompletedJob(job.result);
        localStorage.removeItem(activeJobStorageKey);
        return;
      }

      if (job.status === 'FAILED') {
        localStorage.removeItem(activeJobStorageKey);
        throw new Error(job.message || 'AI processing failed.');
      }

      await waitBeforeNextStatusCheck(1500);
    }
  }

  function waitBeforeNextStatusCheck(delayMilliseconds) {
    return new Promise(function (resolve) {
      setTimeout(resolve, delayMilliseconds);
    });
  }

  function updateProcessingDisplay(job) {
    const progress = Math.max(0, Math.min(100, Number(job.progress) || 0));
    processingSection.classList.remove('d-none');
    processingStatus.innerText = job.message;
    processingProgressBar.style.width = progress + '%';
    processingProgressBar.innerText = progress + '%';
    processingProgressBar.parentElement.setAttribute('aria-valuenow', String(progress));

    if (progress >= 35) {
      audioStatus.innerText = 'Audio extraction completed.';
    }
    if (progress >= 65) {
      transcriptionStatus.innerText = 'Transcription completed.';
    }
    if (progress >= 90) {
      extractionStatus.innerText = 'AI extraction completed. Saving result...';
    }
  }

  function displayCompletedJob(workflowResult) {
    transcript.value = workflowResult.transcript;
    transcriptSection.classList.remove('d-none');
    jsonResult.textContent = JSON.stringify(workflowResult.extraction, null, 2);
    resultSection.classList.remove('d-none');
    extractionStatus.innerText = 'AI extraction completed and saved to Firebase.';
    processingStatus.className = 'alert alert-success';
    processingProgressBar.classList.remove('progress-bar-animated');
  }

  async function restoreActiveJob() {
    const activeJob = getActiveJob();
    if (!activeJob || !activeJob.jobId) {
      return;
    }

    selectedVideoTitle.textContent = activeJob.videoTitle || 'Selected video';
    selectedVideoUrl.textContent = activeJob.videoUrl || '';
    processingSection.classList.remove('d-none');
    resetWorkflow();

    try {
      await pollProcessingJob(activeJob.jobId);
    } catch (error) {
      localStorage.removeItem(activeJobStorageKey);
      processingStatus.className = 'alert alert-danger';
      processingStatus.innerText = error.message || 'Unable to restore processing status.';
    }
  }

  // RESET WORKFLOW
  function resetWorkflow() {
    audioStatus.innerText = 'Waiting...';

    transcriptionStatus.innerText = 'Waiting...';

    extractionStatus.innerText = 'Waiting...';

    transcript.value = '';

    jsonResult.textContent = '';

    processingProgressBar.style.width = '0%';
    processingProgressBar.innerText = '0%';
    processingProgressBar.classList.add('progress-bar-animated');
  }

  // RESET PAGE
  function resetPage() {
    videoList.innerHTML = '';

    videoResultsSection.classList.add('d-none');

    if (!getActiveJob()) {
      processingSection.classList.add('d-none');
    }

    transcriptSection.classList.add('d-none');

    resultSection.classList.add('d-none');

    searchStatus.classList.add('d-none');
  }

  // SEARCH STATUS
  function showSearchStatus(message, type) {
    searchStatus.className = 'alert alert-' + type;

    searchStatus.innerText = message;

    searchStatus.classList.remove('d-none');
  }

  // ERROR HANDLING
  async function getErrorMessage(response) {
    try {
      const contentType = response.headers.get('content-type') || '';

      if (!contentType.includes('application/json')) {
        if (response.url.includes('/login')) {
          return 'Your admin session has expired. Please log in again.';
        }

        return 'Server returned an unexpected HTML response (HTTP ' + response.status + ').';
      }

      const data = await response.json();

      return data.message || data.error || 'Server error: ' + response.status;
    } catch (error) {
      return 'Server error: ' + response.status;
    }
  }

  // COPY JSON
  copyJsonButton.addEventListener('click', async function () {
    const json = jsonResult.textContent;

    if (!json) {
      return;
    }

    try {
      await navigator.clipboard.writeText(json);

      copyJsonButton.innerText = 'Copied!';

      setTimeout(function () {
        copyJsonButton.innerText = 'Copy JSON';
      }, 1500);
    } catch (error) {
      console.error('Unable to copy JSON:', error);
    }
  });

  toggleVideoResults.addEventListener('click', function () {
    const isExpanded = toggleVideoResults.getAttribute('aria-expanded') === 'true';
    setVideoResultsExpanded(!isExpanded);
  });

  function setVideoResultsExpanded(isExpanded) {
    toggleVideoResults.setAttribute('aria-expanded', String(isExpanded));
    videoResultsContent.hidden = !isExpanded;
    toggleVideoResults.querySelector('.toggle-label').textContent = isExpanded ? 'Collapse videos' : 'Expand videos';
    toggleVideoResults.querySelector('.toggle-icon').innerHTML = isExpanded ? '&#8963;' : '&#8964;';
  }

  restoreActiveJob();
});
