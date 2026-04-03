import { useState, useEffect, useOptimistic } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminPrescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [optimisticPrescriptions, removeOptimistic] = useOptimistic(
    prescriptions,
    (current, deletedId) => current.filter(rx => rx.id !== deletedId)
  );

  const loadPrescriptions = () => {
    setLoading(true);
    adminApi.getPrescriptions()
      .then(res => setPrescriptions(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPrescriptions(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this prescription?')) return;
    removeOptimistic(id);
    try { await adminApi.deletePrescription(id); setSuccess('Prescription deleted.'); loadPrescriptions(); }
    catch (err) { setError(err.response?.data?.message || 'Delete failed.'); loadPrescriptions(); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>All Prescriptions</h1>
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Medication</th><th>Patient</th><th>Doctor</th><th>Dosage</th><th>Frequency</th><th>Start</th><th>End</th><th>Active</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticPrescriptions.map((rx) => (
            <tr key={rx.id}>
              <td>{rx.id}</td>
              <td>{rx.medicationName}</td>
              <td>{rx.visit?.patient?.user?.username || rx.visit?.patient?.firstName || '\u2014'}</td>
              <td>{rx.prescribedBy?.name || rx.prescribedBy?.user?.username || '\u2014'}</td>
              <td>{rx.dosage}</td>
              <td>{rx.frequency}</td>
              <td>{rx.startDate}</td>
              <td>{rx.endDate || '\u2014'}</td>
              <td><span className={`status status-${rx.active ? 'active' : 'inactive'}`}>{rx.active ? 'Yes' : 'No'}</span></td>
              <td><button className="btn btn-sm btn-danger" onClick={() => handleDelete(rx.id)}>Delete</button></td>
            </tr>
          ))}
          {optimisticPrescriptions.length === 0 && <tr><td colSpan={10}>No prescriptions found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
