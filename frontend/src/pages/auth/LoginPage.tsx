import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

// ─────────────────────────────────────────────────────────────
// HOW GOOGLE SIGN-IN WORKS ON THE FRONTEND
// ─────────────────────────────────────────────────────────────
//
// Step 1: Load Google's Sign-In library (script tag in index.html)
// Step 2: Initialize google.accounts.id with your Client ID
// Step 3: Render the "Sign in with Google" button
// Step 4: When user clicks → Google popup → user logs in
// Step 5: Google calls our callback with a "credential" (ID token)
// Step 6: We send that ID token to POST /api/auth/google
// Step 7: Backend verifies the token with Google, returns OUR JWT
// Step 8: We save the JWT → user is logged in → redirect to dashboard
//
// The user NEVER types a password — Google handles authentication.
// Our backend just verifies the Google token and issues its own JWT.
// After that, the frontend uses OUR JWT for all API calls.
// ─────────────────────────────────────────────────────────────

// TypeScript declaration for Google's global object
// Google's Sign-In library adds this to the window object
declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
          }) => void;
          renderButton: (
            element: HTMLElement,
            config: { theme: string; size: string; width: number; text: string }
          ) => void;
        };
      };
    };
  }
}

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login, googleLogin } = useAuth();
  const navigate = useNavigate();

  // Role-based redirect — same logic for regular and social login
  // After login, the user goes to their portal's dashboard
  const redirectByRole = useCallback((role: string) => {
    switch (role) {
      case 'ROLE_PATIENT': navigate('/patient/dashboard'); break;
      case 'ROLE_DOCTOR': navigate('/doctor/dashboard'); break;
      case 'ROLE_ADMIN': navigate('/admin/dashboard'); break;
      default: navigate('/');
    }
  }, [navigate]);

  // Regular username/password login
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await login({ username, password });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      redirectByRole(user.role);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.');
    }
  };

  // Google Sign-In callback
  // This runs when Google's popup completes successfully.
  // Google gives us a "credential" which is a signed JWT containing
  // the user's email, name, and profile picture.
  // We DON'T decode it ourselves — we send it to our backend,
  // which verifies it with Google's servers.
  const handleGoogleCallback = useCallback(async (response: { credential: string }) => {
    setError('');
    try {
      // Send Google's token to our backend
      // portalType: "patient" — new Google users default to patient
      // (only matters on first login when auto-creating account)
      await googleLogin({
        googleToken: response.credential,
        portalType: 'patient',
      });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      redirectByRole(user.role);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Google sign-in failed.');
    }
  }, [googleLogin, redirectByRole]);

  // Initialize Google Sign-In when the component mounts
  // This renders the official "Sign in with Google" button
  useEffect(() => {
    // Check if Google's library is loaded
    // (loaded via <script> tag in index.html)
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID || '',
        callback: handleGoogleCallback,
      });

      // Render the Google button inside our div
      const btnDiv = document.getElementById('google-signin-btn');
      if (btnDiv) {
        window.google.accounts.id.renderButton(btnDiv, {
          theme: 'outline',
          size: 'large',
          width: 340,
          text: 'signin_with',
        });
      }
    }
  }, [handleGoogleCallback]);

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Login to LabService</h2>
        {error && <div className="error-message">{error}</div>}

        {/* Regular login form */}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary">Login</button>
        </form>

        {/* Divider between regular and social login */}
        <div className="auth-divider">
          <span>or</span>
        </div>

        {/* Google Sign-In button (rendered by Google's library) */}
        <div className="social-login-section">
          <div id="google-signin-btn" className="google-btn-container"></div>
        </div>

        <div className="auth-links">
          <Link to="/register">Create an account</Link>
          <Link to="/forgot-password">Forgot password?</Link>
        </div>
      </div>
    </div>
  );
}
