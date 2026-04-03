import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminUsers() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadUsers = () => {
    adminApi.getUsers()
      .then(res => setUsers(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadUsers(); }, []);

  const handleEnable = async (id: number) => {
    try { await adminApi.enableUser(id); setSuccess('User enabled.'); loadUsers(); }
    catch (err: any) { setError(err.response?.data?.message || 'Failed.'); }
  };

  const handleDisable = async (id: number) => {
    try { await adminApi.disableUser(id); setSuccess('User disabled.'); loadUsers(); }
    catch (err: any) { setError(err.response?.data?.message || 'Failed.'); }
  };

  const handleRoleChange = async (id: number, currentRole: string) => {
    const roles = ['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN'];
    const newRole = prompt(`Change role to:\n${roles.join('\n')}\n\nCurrent: ${currentRole}`);
    if (!newRole || !roles.includes(newRole)) return;
    try { await adminApi.changeUserRole(id, newRole); setSuccess('Role changed.'); loadUsers(); }
    catch (err: any) { setError(err.response?.data?.message || 'Failed.'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this user permanently?')) return;
    try { await adminApi.deleteUser(id); setSuccess('User deleted.'); loadUsers(); }
    catch (err: any) { setError(err.response?.data?.message || 'Failed.'); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>User Management</h1>
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}
      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Enabled</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {users.map((u: any) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.username}</td>
              <td>{u.email}</td>
              <td>{u.role?.replace('ROLE_', '')}</td>
              <td><span className={`status status-${u.enabled ? 'active' : 'inactive'}`}>{u.enabled ? 'Yes' : 'No'}</span></td>
              <td>
                {u.enabled ? (
                  <button className="btn btn-sm btn-warning" onClick={() => handleDisable(u.id)}>Disable</button>
                ) : (
                  <button className="btn btn-sm btn-primary" onClick={() => handleEnable(u.id)}>Enable</button>
                )}
                <button className="btn btn-sm btn-secondary" onClick={() => handleRoleChange(u.id, u.role)}>Role</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(u.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {users.length === 0 && <tr><td colSpan={6}>No users found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
