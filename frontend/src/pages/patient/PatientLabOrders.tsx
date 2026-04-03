import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientLabOrders() {
  const [labOrders, setLabOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');

  const loadLabOrders = (status: string) => {
    setLoading(true);
    const request = status === 'ALL'
      ? patientApi.getLabOrders()
      : patientApi.getLabOrdersByStatus(status);

    request
      .then(res => setLabOrders(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadLabOrders(statusFilter); }, [statusFilter]);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Lab Orders</h1>
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
      <table className="data-table">
        <thead>
          <tr><th>Test</th><th>Status</th><th>Ordered</th><th>Results</th></tr>
        </thead>
        <tbody>
          {labOrders.map((lab: any) => (
            <tr key={lab.id}>
              <td>{lab.testName}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderDate || lab.orderedDate}</td>
              <td>{lab.results || '—'}</td>
            </tr>
          ))}
          {labOrders.length === 0 && <tr><td colSpan={4}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
