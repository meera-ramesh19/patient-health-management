import { useActionState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function LoginPage() {
  const { login, googleLogin } = useAuth();
  const navigate = useNavigate();

  const redirectByRole = useCallback((role) => {
    switch (role) {
      case 'ROLE_PATIENT': navigate('/patient/dashboard'); break;
      case 'ROLE_DOCTOR': navigate('/doctor/dashboard'); break;
      case 'ROLE_ADMIN': navigate('/admin/dashboard'); break;
      default: navigate('/');
    }
  }, [navigate]);

  // React 19: useActionState replaces manual useState for error/pending
  const [error, submitAction, isPending] = useActionState(async (_prevState, formData) => {
    try {
      await login({
        username: formData.get('username'),
        password: formData.get('password'),
      });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      redirectByRole(user.role);
      return null;
    } catch (err) {
      return err.response?.data?.message || 'Login failed. Check your credentials.';
    }
  }, null);

  const handleGoogleCallback = useCallback(async (response) => {
    try {
      await googleLogin({
        googleToken: response.credential,
        portalType: 'patient',
      });
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      redirectByRole(user.role);
    } catch (err) {
      console.error('Google sign-in failed:', err);
    }
  }, [googleLogin, redirectByRole]);

  useEffect(() => {
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID || '',
        callback: handleGoogleCallback,
      });
      const btnDiv = document.getElementById('google-signin-btn');
      if (btnDiv) {
        window.google.accounts.id.renderButton(btnDiv, {
          theme: 'outline', size: 'large', width: 340, text: 'signin_with',
        });
      }
    }
  }, [handleGoogleCallback]);

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Login to LabService</h2>
        {error && <div className="error-message">{error}</div>}

        {/* React 19: form action instead of onSubmit + preventDefault */}
        <form action={submitAction}>
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isPending}>
            {isPending ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="auth-divider"><span>or</span></div>

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
