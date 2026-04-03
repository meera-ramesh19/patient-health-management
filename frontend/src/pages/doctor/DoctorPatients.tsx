import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorPatients() {
  const [patients, setPatients] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    doctorApi.getPatients()
      .then(res => setPatients(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>My Patients</h1>
      <table className="data-table">
        <thead>
          <tr><th>Name</th><th>Email</th><th>Phone</th><th>Gender</th><th>Blood Group</th></tr>
        </thead>
        <tbody>
          {patients.map((p: any) => (
            <tr key={p.id}>
              <td>{p.user?.username || '—'}</td>
              <td>{p.user?.email || '—'}</td>
              <td>{p.phoneNumber || '—'}</td>
              <td>{p.gender || '—'}</td>
              <td>{p.bloodGroup || '—'}</td>
            </tr>
          ))}
          {patients.length === 0 && <tr><td colSpan={5}>No patients found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
