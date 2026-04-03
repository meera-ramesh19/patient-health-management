import { useState, useEffect, useActionState } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientProfile() {
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patientApi.getProfile()
      .then(res => setProfile(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  // React 19: useActionState for profile update form
  const [state, saveAction, isSaving] = useActionState(async (_prev, formData) => {
    try {
      const updated = {
        phoneNumber: formData.get('phoneNumber'),
        address: formData.get('address'),
        dateOfBirth: formData.get('dateOfBirth'),
        gender: formData.get('gender'),
        bloodGroup: formData.get('bloodGroup'),
      };
      const res = await patientApi.updateProfile(updated);
      setProfile(res.data);
      setEditing(false);
      return { error: null, success: 'Profile updated!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Update failed.', success: null };
    }
  }, { error: null, success: null });

  if (loading) return <div className="loading">Loading...</div>;
  if (!profile) return <div className="error-message">Failed to load profile.</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Profile</h1>
        {!editing && <button className="btn btn-primary" onClick={() => setEditing(true)}>Edit</button>}
      </div>

      {state.error && <div className="error-message">{state.error}</div>}
      {state.success && <div className="success-message">{state.success}</div>}

      {editing ? (
        <form className="profile-form" action={saveAction}>
          <div className="form-group">
            <label>Phone</label>
            <input type="text" name="phoneNumber" defaultValue={profile.phoneNumber || ''} />
          </div>
          <div className="form-group">
            <label>Address</label>
            <input type="text" name="address" defaultValue={profile.address || ''} />
          </div>
          <div className="form-group">
            <label>Date of Birth</label>
            <input type="date" name="dateOfBirth" defaultValue={profile.dateOfBirth || ''} />
          </div>
          <div className="form-group">
            <label>Gender</label>
            <select name="gender" defaultValue={profile.gender || ''}>
              <option value="">Select</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="form-group">
            <label>Blood Group</label>
            <input type="text" name="bloodGroup" defaultValue={profile.bloodGroup || ''} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <div className="profile-details">
          <p><strong>Name:</strong> {profile.user?.username}</p>
          <p><strong>Email:</strong> {profile.user?.email}</p>
          <p><strong>Phone:</strong> {profile.phoneNumber || '\u2014'}</p>
          <p><strong>Address:</strong> {profile.address || '\u2014'}</p>
          <p><strong>Date of Birth:</strong> {profile.dateOfBirth || '\u2014'}</p>
          <p><strong>Gender:</strong> {profile.gender || '\u2014'}</p>
          <p><strong>Blood Group:</strong> {profile.bloodGroup || '\u2014'}</p>
        </div>
      )}
    </div>
  );
}
