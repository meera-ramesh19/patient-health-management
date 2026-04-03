import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getPortalLinks = () => {
    if (!user) return null;

    switch (user.role) {
      case 'ROLE_PATIENT':
        return (
          <>
            <Link to="/patient/dashboard">Dashboard</Link>
            <Link to="/patient/appointments">Appointments</Link>
            <Link to="/patient/visits">Visits</Link>
            <Link to="/patient/prescriptions">Prescriptions</Link>
            <Link to="/patient/lab-orders">Lab Orders</Link>
            <Link to="/patient/my-doctor">My Doctor</Link>
            <Link to="/patient/profile">Profile</Link>
          </>
        );
      case 'ROLE_DOCTOR':
        return (
          <>
            <Link to="/doctor/dashboard">Dashboard</Link>
            <Link to="/doctor/patients">Patients</Link>
            <Link to="/doctor/appointments">Appointments</Link>
            <Link to="/doctor/visits">Visits</Link>
            <Link to="/doctor/prescriptions">Prescriptions</Link>
            <Link to="/doctor/lab-orders">Lab Orders</Link>
            <Link to="/doctor/profile">Profile</Link>
          </>
        );
      case 'ROLE_ADMIN':
        return (
          <>
            <Link to="/admin/dashboard">Dashboard</Link>
            <Link to="/admin/users">Users</Link>
            <Link to="/admin/patients">Patients</Link>
            <Link to="/admin/doctors">Doctors</Link>
            <Link to="/admin/appointments">Appointments</Link>
            <Link to="/admin/visits">Visits</Link>
            <Link to="/admin/prescriptions">Prescriptions</Link>
            <Link to="/admin/lab-orders">Lab Orders</Link>
            <Link to="/admin/pharmacies">Pharmacies</Link>
          </>
        );
      default:
        return null;
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">LabService</Link>
      </div>
      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            {getPortalLinks()}
            <span className="navbar-user">
              {user?.username} ({user?.role.replace('ROLE_', '')})
            </span>
            <button onClick={handleLogout} className="btn btn-logout">Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
