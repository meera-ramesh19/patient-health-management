import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminLabOrders() {
  const [labOrders, setLabOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const loadLabOrders = (status: string) => {
    setLoading(true);
    const request = status === 'ALL'
      ? adminApi.getLabOrders()
      : adminApi.getLabOrdersByStatus(status);

    request
      .then(res => setLabOrders(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadLabOrders(statusFilter); }, [statusFilter]);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this lab order?')) return;
    try { await adminApi.deleteLabOrder(id); setSuccess('Lab order deleted.'); loadLabOrders(statusFilter); }
    catch (err: any) { setError(err.response?.data?.message || 'Delete failed.'); }
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
              onClick={() => setStatusFilter(s)}
            >{s === 'ALL' ? 'All' : s.replace('_', ' ')}</button>
          ))}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Test</th><th>Patient</th><th>Doctor</th><th>Status</th><th>Ordered</th><th>Results</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {labOrders.map((lab: any) => (
            <tr key={lab.id}>
              <td>{lab.id}</td>
              <td>{lab.testName}</td>
              <td>{lab.patient?.user?.username || lab.patient?.firstName || '—'}</td>
              <td>{lab.orderedBy?.name || lab.orderedBy?.user?.username || '—'}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderedDate || lab.orderDate}</td>
              <td>{lab.results || '—'}</td>
              <td><button className="btn btn-sm btn-danger" onClick={() => handleDelete(lab.id)}>Delete</button></td>
            </tr>
          ))}
          {labOrders.length === 0 && <tr><td colSpan={8}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
