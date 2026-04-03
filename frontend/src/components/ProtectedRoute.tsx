import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface Props {
  children: React.ReactNode;
  requiredRole?: string;
}

// Protects routes — redirects to login if not authenticated,
// or to home if the user doesn't have the required role
export default function ProtectedRoute({ children, requiredRole }: Props) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/" />;
  }

  return <>{children}</>;
}
