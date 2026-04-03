import { useState, useEffect, useOptimistic, useTransition } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminLabOrders() {
  const [labOrders, setLabOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isFiltering, startFilterTransition] = useTransition();

  const [optimisticLabs, removeOptimistic] = useOptimistic(
    labOrders,
    (current, deletedId) => current.filter(lab => lab.id !== deletedId)
  );

  const loadLabOrders = (status) => {
    const request = status === 'ALL'
      ? adminApi.getLabOrders()
      : adminApi.getLabOrdersByStatus(status);

    return request.then(res => setLabOrders(res.data));
  };

  useEffect(() => {
    loadLabOrders(statusFilter)
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  // React 19: useTransition for non-blocking filter switch
  const handleStatusChange = (status) => {
    setStatusFilter(status);
    startFilterTransition(async () => {
      try { await loadLabOrders(status); }
      catch (err) { console.error(err); }
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this lab order?')) return;
    removeOptimistic(id);
    try { await adminApi.deleteLabOrder(id); setSuccess('Lab order deleted.'); await loadLabOrders(statusFilter); }
    catch (err) { setError(err.response?.data?.message || 'Delete failed.'); await loadLabOrders(statusFilter); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>All Lab Orders</h1>
        <div className="filter-group">
          {['ALL', 'ORDERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].map(s => (
            <button
              key={s}
              className={`btn btn-sm ${statusFilter === s ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => handleStatusChange(s)}
              disabled={isFiltering}
            >{s === 'ALL' ? 'All' : s.replace('_', ' ')}</button>
          ))}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}
      {isFiltering && <div className="loading">Filtering...</div>}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Test</th><th>Patient</th><th>Doctor</th><th>Status</th><th>Ordered</th><th>Results</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticLabs.map((lab) => (
            <tr key={lab.id}>
              <td>{lab.id}</td>
              <td>{lab.testName}</td>
              <td>{lab.patient?.user?.username || lab.patient?.firstName || '\u2014'}</td>
              <td>{lab.orderedBy?.name || lab.orderedBy?.user?.username || '\u2014'}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderedDate || lab.orderDate}</td>
              <td>{lab.results || '\u2014'}</td>
              <td><button className="btn btn-sm btn-danger" onClick={() => handleDelete(lab.id)}>Delete</button></td>
            </tr>
          ))}
          {optimisticLabs.length === 0 && <tr><td colSpan={8}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
