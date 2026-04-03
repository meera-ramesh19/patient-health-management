import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

// ─────────────────────────────────────────────────────────────
// REGISTER PAGE — Two ways to create an account:
//
// 1. Regular signup: username + email + password + portal type
//    → POST /api/auth/register → creates account → redirect to login
//
// 2. Google signup: click "Sign up with Google"
//    → Google popup → sends ID token to POST /api/auth/google
//    → backend auto-creates account with Google's email/name
//    → returns JWT → user is immediately logged in
//
// The portal type (patient/doctor) matters for both:
//   - Patient accounts are active immediately
//   - Doctor accounts need admin approval (enabled: false)
// ─────────────────────────────────────────────────────────────

// TypeScript declaration for Google's global object
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

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [portalType, setPortalType] = useState('patient');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, googleLogin } = useAuth();
  const navigate = useNavigate();

  // Role-based redirect after social signup
  const redirectByRole = useCallback((role: string) => {
    switch (role) {
      case 'ROLE_PATIENT': navigate('/patient/dashboard'); break;
      case 'ROLE_DOCTOR': navigate('/doctor/dashboard'); break;
      case 'ROLE_ADMIN': navigate('/admin/dashboard'); break;
      default: navigate('/');
    }
  }, [navigate]);

  // Regular form registration
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await register({ username, email, password, portalType });
      setSuccess('Account created! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed.');
    }
  };

  // Google Sign-Up callback
  // Unlike login, here we pass the selected portalType so the backend
  // knows whether to create a patient or doctor account
  const handleGoogleCallback = useCallback(async (response: { credential: string }) => {
    setError('');
    setSuccess('');
    try {
      await googleLogin({
        googleToken: response.credential,
        portalType: portalType,  // Uses the portal type selected in the form
      });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (user.role === 'ROLE_DOCTOR') {
        // Doctor accounts need admin approval
        setSuccess('Account created! Awaiting admin approval...');
        setTimeout(() => navigate('/login'), 3000);
      } else {
        // Patient accounts are active immediately — go to dashboard
        redirectByRole(user.role);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Google sign-up failed.');
    }
  }, [googleLogin, portalType, navigate, redirectByRole]);

  // Initialize Google Sign-In button
  useEffect(() => {
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID || '',
        callback: handleGoogleCallback,
      });

      const btnDiv = document.getElementById('google-signup-btn');
      if (btnDiv) {
        // Clear previous button (in case portalType changed)
        btnDiv.innerHTML = '';
        window.google.accounts.id.renderButton(btnDiv, {
          theme: 'outline',
          size: 'large',
          width: 340,
          text: 'signup_with',
        });
      }
    }
  }, [handleGoogleCallback]);

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Create Account</h2>
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {/* Portal type selector — applies to BOTH regular and Google signup */}
        <div className="form-group">
          <label>Register as</label>
          <select value={portalType} onChange={(e) => setPortalType(e.target.value)}>
            <option value="patient">Patient</option>
            <option value="doctor">Doctor</option>
          </select>
        </div>

        {/* Google Sign-Up button */}
        <div className="social-login-section">
          <div id="google-signup-btn" className="google-btn-container"></div>
        </div>

        <div className="auth-divider">
          <span>or register with email</span>
        </div>

        {/* Regular registration form */}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary">Register</button>
        </form>

        <div className="auth-links">
          <Link to="/login">Already have an account? Login</Link>
        </div>
      </div>
    </div>
  );
}
