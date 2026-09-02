document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('[data-profile-tab]');
    const panels = document.querySelectorAll('[data-profile-panel]');
    tabs.forEach((tab) => tab.addEventListener('click', () => {
        tabs.forEach((item) => item.classList.toggle('active', item === tab));
        panels.forEach((panel) => { panel.hidden = panel.dataset.profilePanel !== tab.dataset.profileTab; });
    }));

    const form = document.querySelector('.profile-panel[method="post"]');
    const fields = form ? Array.from(form.querySelectorAll('input, select, textarea')) : [];
    const originalValues = fields.map((field) => field.value);
    const setEditing = (enabled) => {
        fields.forEach((field) => {
            if (field instanceof HTMLSelectElement) field.disabled = !enabled;
            else field.readOnly = !enabled;
        });
        document.querySelector('[data-profile-edit]').hidden = enabled;
        document.querySelector('[data-profile-save]').hidden = !enabled;
        document.querySelector('[data-profile-cancel]').hidden = !enabled;
    };
    document.querySelector('[data-profile-edit]')?.addEventListener('click', () => setEditing(true));
    document.querySelector('[data-profile-cancel]')?.addEventListener('click', () => {
        fields.forEach((field, index) => { field.value = originalValues[index]; });
        setEditing(false);
    });

    const modal = document.getElementById('avatarUploadModal');
    const avatarButton = document.getElementById('profileAvatarButton');
    const closeButton = document.getElementById('closeAvatarModal');
    const dropzone = document.getElementById('avatarDropzone');
    const fileInput = document.getElementById('avatarFileInput');
    const status = document.getElementById('avatarUploadStatus');
    const avatarImage = document.getElementById('profileAvatarImg');
    const maxSize = 5 * 1024 * 1024;
    const allowedExtensions = ['jpg', 'jpeg', 'png', 'webp'];

    const showStatus = (message, isError = true) => {
        status.textContent = message;
        status.classList.toggle('is-error', isError);
    };
    const validFile = (file) => {
        const extension = file.name.split('.').pop().toLowerCase();
        if (!allowedExtensions.includes(extension)) return 'Use a JPG, PNG, or WebP image.';
        if (file.size > maxSize) return 'Your image must be 5 MB or smaller.';
        return '';
    };
    const upload = async (file) => {
        const error = validFile(file);
        if (error) { showStatus(error); return; }
        const formData = new FormData();
        formData.append('file', file);
        dropzone.classList.add('is-uploading');
        showStatus('Uploading...', false);
        try {
            const response = await fetch('/api/profile/upload-avatar', { method: 'POST', body: formData });
            const payload = await response.json();
            if (!response.ok || !payload.success) throw new Error(payload.message || 'Upload failed.');
            const updatedImageUrl = `${payload.imageUrl}?v=${Date.now()}`;
            avatarImage.src = updatedImageUrl;
            const navbarAvatar = document.getElementById('navProfilePic');
            if (navbarAvatar) {
                navbarAvatar.src = updatedImageUrl;
            }
            modal.close();
            fileInput.value = '';
            showStatus('');
        } catch (uploadError) {
            showStatus(uploadError.message);
        } finally {
            dropzone.classList.remove('is-uploading');
        }
    };

    avatarButton?.addEventListener('click', () => { status.textContent = ''; modal.showModal(); });
    closeButton?.addEventListener('click', () => modal.close());
    dropzone?.addEventListener('click', () => fileInput.click());
    dropzone?.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); fileInput.click(); }
    });
    fileInput?.addEventListener('change', () => { if (fileInput.files[0]) upload(fileInput.files[0]); });
    ['dragenter', 'dragover'].forEach((eventName) => dropzone?.addEventListener(eventName, (event) => {
        event.preventDefault(); dropzone.classList.add('is-dragging');
    }));
    ['dragleave', 'drop'].forEach((eventName) => dropzone?.addEventListener(eventName, (event) => {
        event.preventDefault(); dropzone.classList.remove('is-dragging');
    }));
    dropzone?.addEventListener('drop', (event) => { if (event.dataTransfer.files[0]) upload(event.dataTransfer.files[0]); });
    document.getElementById('switch-account-button')?.addEventListener('click', async () => {
        const response = await fetch('/api/auth/logout', { method: 'POST' });
        if (response.ok) window.location.href = '/login';
    });
});
