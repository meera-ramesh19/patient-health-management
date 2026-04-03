import { useState, useEffect, useActionState, useOptimistic } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorLabOrders() {
  const [labOrders, setLabOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const [optimisticLabs, updateOptimistic] = useOptimistic(
    labOrders,
    (current, { id, status }) => current.map(lab =>
      lab.id === id ? { ...lab, status } : lab
    )
  );

  const loadLabOrders = () => {
    doctorApi.getLabOrders()
      .then(res => setLabOrders(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadLabOrders(); }, []);

  // React 19: useActionState for order form
  const [orderState, orderAction, isOrdering] = useActionState(async (_prev, formData) => {
    try {
      await doctorApi.orderLab({
        patientId: Number(formData.get('patientId')),
        testName: formData.get('testName'),
        notes: formData.get('notes'),
      });
      setShowForm(false);
      loadLabOrders();
      return { error: null, success: 'Lab ordered!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Failed to order lab.', success: null };
    }
  }, { error: null, success: null });

  const handleStatusUpdate = async (id, status) => {
    updateOptimistic({ id, status });
    try {
      await doctorApi.updateLabStatus(id, status);
      loadLabOrders();
    } catch (err) {
      loadLabOrders();
    }
  };

  const handleAddResults = async (id) => {
    const results = window.prompt('Enter lab results:');
    if (!results) return;
    try {
      await doctorApi.addLabResults(id, results);
      loadLabOrders();
    } catch (err) {
      console.error(err);
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

      {orderState.error && <div className="error-message">{orderState.error}</div>}
      {orderState.success && <div className="success-message">{orderState.success}</div>}

      {showForm && (
        <form className="inline-form" action={orderAction}>
          <div className="form-group">
            <label>Patient ID</label>
            <input type="number" name="patientId" required />
          </div>
          <div className="form-group">
            <label>Test Name</label>
            <input type="text" name="testName" required />
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea name="notes" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isOrdering}>
            {isOrdering ? 'Ordering...' : 'Order'}
          </button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Test</th><th>Patient</th><th>Status</th><th>Ordered</th><th>Results</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticLabs.map((lab) => (
            <tr key={lab.id}>
              <td>{lab.testName}</td>
              <td>{lab.patient?.user?.username || lab.patientId}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderDate}</td>
              <td>{lab.results || '\u2014'}</td>
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
          {optimisticLabs.length === 0 && <tr><td colSpan={6}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
