import api from './client';

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),

  getUsers: () => api.get('/admin/users'),
  getUserById: (id) => api.get(`/admin/users/${id}`),
  enableUser: (id) => api.put(`/admin/users/${id}/enable`),
  disableUser: (id) => api.put(`/admin/users/${id}/disable`),
  changeUserRole: (id, role) =>
    api.put(`/admin/users/${id}/role`, JSON.stringify(role), { headers: { 'Content-Type': 'application/json' } }),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),

  getPatients: () => api.get('/admin/patients'),
  getPatientById: (id) => api.get(`/admin/patients/${id}`),
  searchPatients: (name) => api.get(`/admin/patients/search?name=${encodeURIComponent(name)}`),
  createPatient: (data) => api.post('/admin/patients', data),
  updatePatient: (id, data) => api.put(`/admin/patients/${id}`, data),
  assignDoctor: (patientId, doctorId) =>
    api.put(`/admin/patients/${patientId}/assign-doctor/${doctorId}`),
  deletePatient: (id) => api.delete(`/admin/patients/${id}`),

  getDoctors: () => api.get('/admin/doctors'),
  getDoctorById: (id) => api.get(`/admin/doctors/${id}`),
  searchDoctors: (name) => api.get(`/admin/doctors/search?name=${encodeURIComponent(name)}`),
  createDoctor: (data) => api.post('/admin/doctors', data),
  updateDoctor: (id, data) => api.put(`/admin/doctors/${id}`, data),
  deleteDoctor: (id) => api.delete(`/admin/doctors/${id}`),

  getVisits: () => api.get('/admin/visits'),
  getVisitById: (id) => api.get(`/admin/visits/${id}`),
  getVisitsByDateRange: (start, end) =>
    api.get(`/admin/visits/range?start=${start}&end=${end}`),
  deleteVisit: (id) => api.delete(`/admin/visits/${id}`),

  getPrescriptions: () => api.get('/admin/prescriptions'),
  getPrescriptionById: (id) => api.get(`/admin/prescriptions/${id}`),
  deletePrescription: (id) => api.delete(`/admin/prescriptions/${id}`),

  getLabOrders: () => api.get('/admin/lab-orders'),
  getLabOrderById: (id) => api.get(`/admin/lab-orders/${id}`),
  getLabOrdersByStatus: (status) => api.get(`/admin/lab-orders/status/${status}`),
  deleteLabOrder: (id) => api.delete(`/admin/lab-orders/${id}`),

  getAppointments: () => api.get('/admin/appointments'),
  getAppointmentById: (id) => api.get(`/admin/appointments/${id}`),
  cancelAppointment: (id) => api.put(`/admin/appointments/${id}/cancel`),
  deleteAppointment: (id) => api.delete(`/admin/appointments/${id}`),

  getPharmacies: () => api.get('/admin/pharmacies'),
  getPharmacyById: (id) => api.get(`/admin/pharmacies/${id}`),
  searchPharmacies: (name) => api.get(`/admin/pharmacies/search?name=${encodeURIComponent(name)}`),
  createPharmacy: (data) => api.post('/admin/pharmacies', data),
  updatePharmacy: (id, data) => api.put(`/admin/pharmacies/${id}`, data),
  deletePharmacy: (id) => api.delete(`/admin/pharmacies/${id}`),
};
