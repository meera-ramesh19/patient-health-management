import { useState, useEffect, useTransition } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientLabOrders() {
  const [labOrders, setLabOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isFiltering, startFilterTransition] = useTransition();

  const loadLabOrders = (status) => {
    const request = status === 'ALL'
      ? patientApi.getLabOrders()
      : patientApi.getLabOrdersByStatus(status);

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
      try {
        await loadLabOrders(status);
      } catch (err) {
        console.error(err);
      }
    });
  };

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
              onClick={() => handleStatusChange(s)}
              disabled={isFiltering}
            >{s === 'ALL' ? 'All' : s.replace('_', ' ')}</button>
          ))}
        </div>
      </div>
      {isFiltering && <div className="loading">Filtering...</div>}
      <table className="data-table">
        <thead>
          <tr><th>Test</th><th>Status</th><th>Ordered</th><th>Results</th></tr>
        </thead>
        <tbody>
          {labOrders.map((lab) => (
            <tr key={lab.id}>
              <td>{lab.testName}</td>
              <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
              <td>{lab.orderDate || lab.orderedDate}</td>
              <td>{lab.results || '\u2014'}</td>
            </tr>
          ))}
          {labOrders.length === 0 && <tr><td colSpan={4}>No lab orders found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
