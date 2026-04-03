import { useActionState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { authApi } from '../../api/auth';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const navigate = useNavigate();

  // React 19: useActionState for form submission
  const [state, submitAction, isPending] = useActionState(async (_prevState, formData) => {
    const newPassword = formData.get('newPassword');
    const confirmPassword = formData.get('confirmPassword');
    if (newPassword !== confirmPassword) {
      return { error: 'Passwords do not match.', message: null };
    }
    try {
      await authApi.resetPassword({ token, newPassword });
      setTimeout(() => navigate('/login'), 2000);
      return { error: null, message: 'Password reset successfully! Redirecting to login...' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Reset failed. Token may be expired.', message: null };
    }
  }, { error: null, message: null });

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Reset Password</h2>
        {state.error && <div className="error-message">{state.error}</div>}
        {state.message && <div className="success-message">{state.message}</div>}
        {!token ? (
          <div className="error-message">Invalid reset link. No token provided.</div>
        ) : (
          <form action={submitAction}>
            <div className="form-group">
              <label>New Password</label>
              <input type="password" name="newPassword" required />
            </div>
            <div className="form-group">
              <label>Confirm Password</label>
              <input type="password" name="confirmPassword" required />
            </div>
            <button type="submit" className="btn btn-primary" disabled={isPending}>
              {isPending ? 'Resetting...' : 'Reset Password'}
            </button>
          </form>
        )}
        <div className="auth-links">
          <Link to="/login">Back to Login</Link>
        </div>
      </div>
    </div>
  );
}
