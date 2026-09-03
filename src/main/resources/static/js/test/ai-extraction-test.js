const extractButton = document.getElementById('extractButton');
const clearButton = document.getElementById('clearButton');
const transcriptInput = document.getElementById('transcript');
const statusMessage = document.getElementById('status-message');

extractButton.addEventListener('click', extractInformation);
clearButton.addEventListener('click', clearForm);

async function extractInformation() {
  const transcript = transcriptInput.value.trim();

  if (!transcript) {
    updateStatus('Please enter a transcript.', 'error');
    return;
  }

  setProcessing(true);

  updateStatus('Sending transcript to Gemini...', 'processing');

  try {
    const response = await fetch('/api/ai/extract', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        transcript: transcript,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();

      throw new Error(errorText || 'AI extraction failed.');
    }

    const result = await response.json();

    displayResult(result);

    updateStatus('AI extraction completed successfully.', 'success');
  } catch (error) {
    console.error('AI extraction error:', error);

    updateStatus('Error: ' + error.message, 'error');
  } finally {
    setProcessing(false);
  }
}

function displayResult(result) {
  document.getElementById('name').textContent = displayValue(result.name);

  document.getElementById('address').textContent = displayValue(result.address);

  document.getElementById('state').textContent = displayValue(result.state);

  document.getElementById('city').textContent = displayValue(result.city);

  document.getElementById('location').textContent = displayValue(result.location);

  document.getElementById('description').textContent = displayValue(result.description);

  document.getElementById('operatingHour').textContent = displayValue(result.operatingHour);

  document.getElementById('rawJson').textContent = JSON.stringify(result, null, 4);
}

function displayValue(value) {
  if (value === null || value === undefined || value === '') {
    return 'null';
  }

  return value;
}

function updateStatus(message, type = '') {
  statusMessage.textContent = message;

  statusMessage.className = 'status-message';

  if (type) {
    statusMessage.classList.add(type);
  }
}

function setProcessing(processing) {
  extractButton.disabled = processing;

  if (processing) {
    extractButton.textContent = 'Extracting...';
  } else {
    extractButton.textContent = 'Extract Heritage Information';
  }
}

function clearForm() {
  transcriptInput.value = '';

  document.getElementById('name').textContent = '-';
  document.getElementById('address').textContent = '-';
  document.getElementById('state').textContent = '-';
  document.getElementById('city').textContent = '-';
  document.getElementById('location').textContent = '-';
  document.getElementById('description').textContent = '-';
  document.getElementById('operatingHour').textContent = '-';

  document.getElementById('rawJson').textContent = 'No result yet.';

  updateStatus('Ready');
}
