(function () {
  const MAX_CACHED_LOCATION_AGE_MS = 30_000;
  const MAX_CACHED_LOCATION_ACCURACY_METERS = 50;
  const FRESH_LOCATION_TIMEOUT_MS = 30_000;
  const BROWSER_LOCATION_CACHE_AGE_MS = 10_000;

  const locateUser = () =>
    new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Location services are not supported by this browser.'));
        return;
      }
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: FRESH_LOCATION_TIMEOUT_MS,
        maximumAge: BROWSER_LOCATION_CACHE_AGE_MS,
      });
    });

  const isUsableCachedLocation = (location) =>
    Number.isFinite(location?.latitude) &&
    Number.isFinite(location?.longitude) &&
    Number.isFinite(location?.accuracy) &&
    location.accuracy <= MAX_CACHED_LOCATION_ACCURACY_METERS &&
    Number.isFinite(location?.timestamp) &&
    Date.now() - location.timestamp >= 0 &&
    Date.now() - location.timestamp <= MAX_CACHED_LOCATION_AGE_MS;

  const locationErrorMessage = (error) => {
    if (error?.code === 1) {
      return 'Location permission was denied. Please allow location access and try again.';
    }
    if (error?.code === 2) {
      return 'Your location is currently unavailable. Check location services and try again.';
    }
    if (error?.code === 3) {
      return 'Unable to obtain an accurate location in time. Move to an open area and try again.';
    }
    return error?.message || 'Unable to determine your location.';
  };

  const parseResponse = async (response) => {
    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      throw new Error('The server returned an invalid response. Please sign in again.');
    }
    return response.json();
  };

  const markCheckedIn = (button, statusElement) => {
    if (button) {
      button.disabled = true;
      button.textContent = 'Checked In Today';
      button.dataset.checkedInToday = 'true';
    }
    if (statusElement) {
      statusElement.textContent = 'You have already checked in at this business today.';
      statusElement.classList.add('check-in-success');
    }
  };

  const refreshStatus = async (businessId, button, statusElement) => {
    if (!businessId || !button) return false;
    try {
      const response = await fetch(`/api/checkin/status?businessId=${encodeURIComponent(businessId)}`);
      if (!response.ok) return false;
      const result = await response.json();
      if (result.checkedInToday) markCheckedIn(button, statusElement);
      return Boolean(result.checkedInToday);
    } catch (error) {
      console.warn('Unable to load check-in status:', error);
      return false;
    }
  };

  const checkIn = async (business, button, statusElement, cachedLocation = null) => {
    if (!business?.businessId || button?.disabled) return;
    const originalLabel = button?.textContent || 'Check In';
    const useCachedLocation = isUsableCachedLocation(cachedLocation);

    if (button) {
      button.disabled = true;
      button.textContent = useCachedLocation ? 'Saving check-in...' : 'Finding location...';
    }
    if (statusElement) {
      statusElement.textContent = useCachedLocation
        ? 'Using your current map location...'
        : 'Requesting your current GPS location...';
    }

    try {
      const position = useCachedLocation
        ? {
            coords: {
              latitude: cachedLocation.latitude,
              longitude: cachedLocation.longitude,
            },
          }
        : await locateUser();
      if (button) button.textContent = 'Saving check-in...';
      const response = await fetch('/api/checkin', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          businessId: business.businessId,
          userLatitude: position.coords.latitude,
          userLongitude: position.coords.longitude,
        }),
      });
      const result = await parseResponse(response);
      if (!response.ok || !result.success) {
        if (result.message?.includes('already checked in')) {
          markCheckedIn(button, statusElement);
          window.alert(result.message);
          return;
        }
        throw new Error(result.message || 'The check-in could not be completed.');
      }

      const message = `Checked in successfully. You earned ${result.pointsEarned} points.`;
      markCheckedIn(button, statusElement);
      if (statusElement) {
        statusElement.textContent = `${message} Total points: ${result.currentPoints}.`;
        statusElement.classList.add('check-in-success');
      }
      window.dispatchEvent(
        new CustomEvent('warisango:check-in-complete', {
          detail: { ...result, businessId: business.businessId },
        })
      );
      window.alert(message);
    } catch (error) {
      const message = locationErrorMessage(error);
      if (statusElement) {
        statusElement.textContent = message;
        statusElement.classList.remove('check-in-success');
      }
      if (button) {
        button.disabled = false;
        button.textContent = originalLabel;
      }
      window.alert(message);
    }
  };

  const initializeDetailButton = () => {
    const button = document.querySelector('[data-check-in-business]');
    if (!button) return;
    if (document.getElementById('guest-gate-modal')) return;
    const statusElement = document.getElementById('check-in-status');
    button.addEventListener('click', () =>
      checkIn(
        {
          businessId: button.dataset.businessId,
        },
        button,
        statusElement
      )
    );
    refreshStatus(button.dataset.businessId, button, statusElement);
  };

  window.WarisanGoCheckIn = { checkIn, refreshStatus };
  document.addEventListener('DOMContentLoaded', initializeDetailButton);
})();
