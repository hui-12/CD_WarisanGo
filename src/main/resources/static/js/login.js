import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getAuth, signInWithPopup, GoogleAuthProvider } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-auth.js";

// Fetch this configuration from your backend or inject it securely
const firebaseConfig = {
    apiKey: "YOUR_PUBLIC_API_KEY",
    authDomain: "warisango.firebaseapp.com",
    projectId: "warisango"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

const handleLogin = async (role) => {
    try {
        const result = await signInWithPopup(auth, provider);
        const idToken = await result.user.getIdToken();

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                idToken: idToken,
                role: role
            })
        });

        if (response.ok) {
            window.location.href = role === 'admin' ? '/ai-discovery' : '/';
        } else {
            console.error("Authentication failed on the server.");
            alert("Login failed. Please verify your credentials.");
        }
    } catch (error) {
        console.error("Error during Google Sign-In:", error);
    }
};

document.getElementById('btn-login-tourist').addEventListener('click', () => handleLogin('tourist'));
document.getElementById('btn-login-admin').addEventListener('click', () => handleLogin('admin'));   