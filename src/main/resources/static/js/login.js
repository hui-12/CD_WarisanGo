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

const handleLogin = async () => {
    try {
        await signOut(auth);
        const result = await signInWithPopup(auth, provider);
        const idToken = await result.user.getIdToken();

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                idToken: idToken
            })
        });

        if (response.ok) {
            const user = await response.json();
            window.location.href = user.role === 'admin' ? '/ai-discovery' : '/';
        } else {
            console.error("Authentication failed on the server.");
            alert("Login failed. Please verify your credentials.");
        }
    } catch (error) {
        console.error("Error during Google Sign-In:", error);
    }
};

document.getElementById('btn-login-tourist').addEventListener('click', handleLogin);
document.getElementById('btn-login-admin').addEventListener('click', handleLogin);
