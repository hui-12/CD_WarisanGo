const switchAccountButton = document.getElementById('switch-account-button');

switchAccountButton.addEventListener('click', async () => {
    try {
        const response = await fetch('/api/auth/logout', { method: 'POST' });
        if (!response.ok) {
            throw new Error('Unable to end the current session.');
        }

        window.location.href = '/login';
    } catch (error) {
        console.error('Account switch failed:', error);
        alert('Unable to switch accounts. Please try again.');
    }
});
