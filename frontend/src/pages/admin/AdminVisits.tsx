import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminVisits() {
  const [visits, setVisits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadVisits = () => {
    setLoading(true);
    adminApi.getVisits()
      .then(res => setVisits(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadVisits(); }, []);

  const handleDateFilter = async () => {
    if (!startDate || !endDate) return;
    setLoading(true);
    try { const res = await adminApi.getVisitsByDateRange(startDate, endDate); setVisits(res.data); }
    catch (err: any) { setError(err.response?.data?.message || 'Filter failed.'); }
    finally { setLoading(false); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this visit record?')) return;
    try { await adminApi.deleteVisit(id); setSuccess('Visit deleted.'); loadVisits(); }
    catch (err: any) { setError(err.response?.data?.message || 'Delete failed.'); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>All Visits</h1>
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div className="search-bar">
        <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} />
        <span style={{alignSelf:'center'}}>to</span>
        <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} />
        <button className="btn btn-sm btn-primary" onClick={handleDateFilter}>Filter</button>
        {(startDate || endDate) && <button className="btn btn-sm btn-secondary" onClick={() => { setStartDate(''); setEndDate(''); loadVisits(); }}>Clear</button>}
      </div>

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Date</th><th>Patient</th><th>Doctor</th><th>Diagnosis</th><th>Notes</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {visits.map((v: any) => (
            <tr key={v.id}>
              <td>{v.id}</td>
              <td>{v.visitDate}</td>
              <td>{v.patient?.user?.username || v.patient?.firstName || '—'}</td>
              <td>{v.doctor?.name || v.doctor?.user?.username || '—'}</td>
              <td>{v.diagnosis || '—'}</td>
              <td>{v.notes || '—'}</td>
              <td><button className="btn btn-sm btn-danger" onClick={() => handleDelete(v.id)}>Delete</button></td>
            </tr>
          ))}
          {visits.length === 0 && <tr><td colSpan={7}>No visits found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
