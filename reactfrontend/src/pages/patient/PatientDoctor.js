import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientDoctor() {
  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    patientApi.getMyDoctor()
      .then(res => setDoctor(res.data))
      .catch(err => {
        if (err.response?.status === 404 || !err.response?.data) {
          setError('No primary doctor assigned yet.');
        } else {
          setError(err.response?.data?.message || 'Failed to load doctor info.');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>My Doctor</h1>
      {error && <div className="error-message">{error}</div>}
      {doctor && (
        <div className="profile-details">
          <p><strong>Name:</strong> {doctor.name || doctor.user?.username || '\u2014'}</p>
          <p><strong>Email:</strong> {doctor.email || doctor.user?.email || '\u2014'}</p>
          <p><strong>Specialization:</strong> {doctor.specialization || '\u2014'}</p>
          <p><strong>Phone:</strong> {doctor.phoneNumber || '\u2014'}</p>
          <p><strong>Hospital:</strong> {doctor.hospitalAffiliation || '\u2014'}</p>
          <p><strong>Experience:</strong> {doctor.yearsOfExperience ? `${doctor.yearsOfExperience} years` : '\u2014'}</p>
          <p><strong>Consultation Fee:</strong> {doctor.consultationFee ? `$${doctor.consultationFee}` : '\u2014'}</p>
        </div>
      )}
    </div>
  );
}
