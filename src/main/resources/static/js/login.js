import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getAuth, signInWithPopup, GoogleAuthProvider, signOut } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-auth.js";


const firebaseConfig = {
  apiKey: "AIzaSyBXQ0QR6fkW5cwbnUqkFF78-0-zeGeFu5M",
  authDomain: "warisango.firebaseapp.com",
  projectId: "warisango",
  storageBucket: "warisango.firebasestorage.app",
  messagingSenderId: "880767167288",
  appId: "1:880767167288:web:9e352fe9321030d6302fcd",
  measurementId: "G-FZ8F22YPDM"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();
provider.setCustomParameters({ prompt: 'select_account' });
const loginButtons = document.querySelectorAll('[data-role]');
const loginStatus = document.getElementById('login-status');
const loginEndpoint = document.body.dataset.loginEndpoint;
const loginRedirect = document.body.dataset.loginRedirect;

const setLoading = (isLoading) => {
    loginButtons.forEach((button) => {
        button.disabled = isLoading;
    });
};

const showStatus = (message) => {
    loginStatus.textContent = message;
};

const handleLogin = async (event) => {
    const selectedRole = event.currentTarget.dataset.role;

    try {
        setLoading(true);
        showStatus(`Opening Google sign-in for ${selectedRole} access...`);
        await signOut(auth);
        const result = await signInWithPopup(auth, provider);
        const idToken = await result.user.getIdToken();

        const response = await fetch(loginEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                idToken: idToken
            })
        });

        if (response.ok) {
            await response.json();
            window.location.href = loginRedirect;
        } else {
            showStatus('Login failed. Please verify that your account has access.');
        }
    } catch (error) {
        if (error.code !== 'auth/popup-closed-by-user') {
            console.error('Error during Google Sign-In:', error);
            showStatus('Google sign-in could not be completed. Please try again.');
        } else {
            showStatus('Sign-in was cancelled.');
        }
    } finally {
        setLoading(false);
    }
};

loginButtons.forEach((button) => {
    button.addEventListener('click', handleLogin);
});
