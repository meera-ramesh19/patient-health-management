import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorLabOrders() {
  const [labOrders, setLabOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ patientId: '', testName: '', notes: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadLabOrders = () => {
    doctorApi.getLabOrders()
      .then(res => setLabOrders(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadLabOrders(); }, []);

  const handleOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await doctorApi.orderLab({
        patientId: Number(form.patientId),
        testName: form.testName,
        notes: form.notes,
      });
      setSuccess('Lab ordered!');
      setShowForm(false);
      setForm({ patientId: '', testName: '', notes: '' });
      loadLabOrders();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to order lab.');
    }
  };

  const handleStatusUpdate = async (id: number, status: string) => {
    try {
      await doctorApi.updateLabStatus(id, status);
      loadLabOrders();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update status.');
    }
  };

  const handleAddResults = async (id: number) => {
    const results = prompt('Enter lab results:');
    if (!results) return;
    try {
      await doctorApi.addLabResults(id, results);
      loadLabOrders();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add results.');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Lab Orders</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Order Lab Test'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {showForm && (
        <form className="inline-form" onSubmit={handleOrder}>
          <div className="form-group">
            <label>Patient ID</label>
            <input type="number" value={form.patientId} onChange={e => setForm({...form, patientId: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Test Name</label>
            <input type="text" value={form.testName} onChange={e => setForm({...form, testName: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea value={form.notes} onChange={e => setForm({...form, notes: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary">Order</button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Test</th><th>Patient</th><th>Status</th><th>Ordered</th><th>Results</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {labOrders.map((lab: any) => (
            <tr key={lab.id}>
              <td>{lab.testName}</td>
              <td>{lab.patient?.user?.username || lab.patientId}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderDate}</td>
              <td>{lab.results || '—'}</td>
              <td>
                {lab.status === 'PENDING' && (
                  <button className="btn btn-sm btn-secondary" onClick={() => handleStatusUpdate(lab.id, 'IN_PROGRESS')}>Start</button>
                )}
                {lab.status === 'IN_PROGRESS' && (
                  <>
                    <button className="btn btn-sm btn-secondary" onClick={() => handleAddResults(lab.id)}>Add Results</button>
                    <button className="btn btn-sm btn-primary" onClick={() => handleStatusUpdate(lab.id, 'COMPLETED')}>Complete</button>
                  </>
                )}
              </td>
            </tr>
          ))}
          {labOrders.length === 0 && <tr><td colSpan={6}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
