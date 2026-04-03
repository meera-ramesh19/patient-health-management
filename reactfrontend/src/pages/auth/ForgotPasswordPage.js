import { useActionState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../../api/auth';

export default function ForgotPasswordPage() {
  // React 19: useActionState for form + error/success/pending in one hook
  const [state, submitAction, isPending] = useActionState(async (_prevState, formData) => {
    try {
      await authApi.forgotPassword({ email: formData.get('email') });
      return { error: null, message: 'If that email exists, a reset link has been sent.' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Something went wrong.', message: null };
    }
  }, { error: null, message: null });

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Forgot Password</h2>
        {state.error && <div className="error-message">{state.error}</div>}
        {state.message && <div className="success-message">{state.message}</div>}
        <form action={submitAction}>
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" name="email" required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isPending}>
            {isPending ? 'Sending...' : 'Send Reset Link'}
          </button>
        </form>
        <div className="auth-links">
          <Link to="/login">Back to Login</Link>
        </div>
      </div>
    </div>
  );
}
