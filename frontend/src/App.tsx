import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';

// Patient pages
import PatientDashboard from './pages/patient/PatientDashboard';
import PatientAppointments from './pages/patient/PatientAppointments';
import PatientVisits from './pages/patient/PatientVisits';
import PatientPrescriptions from './pages/patient/PatientPrescriptions';
import PatientLabOrders from './pages/patient/PatientLabOrders';
import PatientProfile from './pages/patient/PatientProfile';
import PatientDoctor from './pages/patient/PatientDoctor';

// Doctor pages
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import DoctorPatients from './pages/doctor/DoctorPatients';
import DoctorAppointments from './pages/doctor/DoctorAppointments';
import DoctorVisits from './pages/doctor/DoctorVisits';
import DoctorPrescriptions from './pages/doctor/DoctorPrescriptions';
import DoctorLabOrders from './pages/doctor/DoctorLabOrders';
import DoctorProfile from './pages/doctor/DoctorProfile';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from './pages/admin/AdminUsers';
import AdminPatients from './pages/admin/AdminPatients';
import AdminDoctors from './pages/admin/AdminDoctors';
import AdminAppointments from './pages/admin/AdminAppointments';
import AdminVisits from './pages/admin/AdminVisits';
import AdminPrescriptions from './pages/admin/AdminPrescriptions';
import AdminLabOrders from './pages/admin/AdminLabOrders';
import AdminPharmacies from './pages/admin/AdminPharmacies';

export default function App() {
  return (
    <>
      <Navbar />
      <main className="main-content">
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Patient routes */}
          <Route path="/patient/dashboard" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientDashboard /></ProtectedRoute>} />
          <Route path="/patient/appointments" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientAppointments /></ProtectedRoute>} />
          <Route path="/patient/visits" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientVisits /></ProtectedRoute>} />
          <Route path="/patient/prescriptions" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientPrescriptions /></ProtectedRoute>} />
          <Route path="/patient/lab-orders" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientLabOrders /></ProtectedRoute>} />
          <Route path="/patient/profile" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientProfile /></ProtectedRoute>} />
          <Route path="/patient/my-doctor" element={<ProtectedRoute requiredRole="ROLE_PATIENT"><PatientDoctor /></ProtectedRoute>} />

          {/* Doctor routes */}
          <Route path="/doctor/dashboard" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorDashboard /></ProtectedRoute>} />
          <Route path="/doctor/patients" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorPatients /></ProtectedRoute>} />
          <Route path="/doctor/appointments" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorAppointments /></ProtectedRoute>} />
          <Route path="/doctor/visits" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorVisits /></ProtectedRoute>} />
          <Route path="/doctor/prescriptions" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorPrescriptions /></ProtectedRoute>} />
          <Route path="/doctor/lab-orders" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorLabOrders /></ProtectedRoute>} />
          <Route path="/doctor/profile" element={<ProtectedRoute requiredRole="ROLE_DOCTOR"><DoctorProfile /></ProtectedRoute>} />

          {/* Admin routes */}
          <Route path="/admin/dashboard" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminUsers /></ProtectedRoute>} />
          <Route path="/admin/patients" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminPatients /></ProtectedRoute>} />
          <Route path="/admin/doctors" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminDoctors /></ProtectedRoute>} />
          <Route path="/admin/appointments" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminAppointments /></ProtectedRoute>} />
          <Route path="/admin/visits" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminVisits /></ProtectedRoute>} />
          <Route path="/admin/prescriptions" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminPrescriptions /></ProtectedRoute>} />
          <Route path="/admin/lab-orders" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminLabOrders /></ProtectedRoute>} />
          <Route path="/admin/pharmacies" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><AdminPharmacies /></ProtectedRoute>} />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </main>
    </>
  );
}
