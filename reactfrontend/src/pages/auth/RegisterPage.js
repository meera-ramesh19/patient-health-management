import { useState, useActionState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const [portalType, setPortalType] = useState('patient');
  const { register, googleLogin } = useAuth();
  const navigate = useNavigate();

  const redirectByRole = useCallback((role) => {
    switch (role) {
      case 'ROLE_PATIENT': navigate('/patient/dashboard'); break;
      case 'ROLE_DOCTOR': navigate('/doctor/dashboard'); break;
      case 'ROLE_ADMIN': navigate('/admin/dashboard'); break;
      default: navigate('/');
    }
  }, [navigate]);

  // React 19: useActionState for form submission
  const [state, submitAction, isPending] = useActionState(async (_prevState, formData) => {
    try {
      await register({
        username: formData.get('username'),
        email: formData.get('email'),
        password: formData.get('password'),
        portalType,
      });
      setTimeout(() => navigate('/login'), 2000);
      return { error: null, success: 'Account created! Redirecting to login...' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Registration failed.', success: null };
    }
  }, { error: null, success: null });

  const handleGoogleCallback = useCallback(async (response) => {
    try {
      await googleLogin({ googleToken: response.credential, portalType });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (user.role === 'ROLE_DOCTOR') {
        setTimeout(() => navigate('/login'), 3000);
      } else {
        redirectByRole(user.role);
      }
    } catch (err) {
      console.error('Google sign-up failed:', err);
    }
  }, [googleLogin, portalType, navigate, redirectByRole]);

  useEffect(() => {
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID || '',
        callback: handleGoogleCallback,
      });
      const btnDiv = document.getElementById('google-signup-btn');
      if (btnDiv) {
        btnDiv.innerHTML = '';
        window.google.accounts.id.renderButton(btnDiv, {
          theme: 'outline', size: 'large', width: 340, text: 'signup_with',
        });
      }
    }
  }, [handleGoogleCallback]);

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Create Account</h2>
        {state.error && <div className="error-message">{state.error}</div>}
        {state.success && <div className="success-message">{state.success}</div>}

        <div className="form-group">
          <label>Register as</label>
          <select value={portalType} onChange={(e) => setPortalType(e.target.value)}>
            <option value="patient">Patient</option>
            <option value="doctor">Doctor</option>
          </select>
        </div>

        <div className="social-login-section">
          <div id="google-signup-btn" className="google-btn-container"></div>
        </div>

        <div className="auth-divider"><span>or register with email</span></div>

        {/* React 19: form action */}
        <form action={submitAction}>
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" name="email" required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isPending}>
            {isPending ? 'Registering...' : 'Register'}
          </button>
        </form>

        <div className="auth-links">
          <Link to="/login">Already have an account? Login</Link>
        </div>
      </div>
    </div>
  );
}
