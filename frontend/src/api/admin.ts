import api from './client';

export const adminApi = {
  // ==================== DASHBOARD ====================
  getDashboard: () => api.get('/admin/dashboard'),

  // ==================== USER MANAGEMENT ====================
  getUsers: () => api.get('/admin/users'),
  getUserById: (id: number) => api.get(`/admin/users/${id}`),
  enableUser: (id: number) => api.put(`/admin/users/${id}/enable`),
  disableUser: (id: number) => api.put(`/admin/users/${id}/disable`),
  changeUserRole: (id: number, role: string) =>
    api.put(`/admin/users/${id}/role`, JSON.stringify(role), { headers: { 'Content-Type': 'application/json' } }),
  deleteUser: (id: number) => api.delete(`/admin/users/${id}`),

  // ==================== PATIENT MANAGEMENT ====================
  getPatients: () => api.get('/admin/patients'),
  getPatientById: (id: number) => api.get(`/admin/patients/${id}`),
  searchPatients: (name: string) => api.get(`/admin/patients/search?name=${encodeURIComponent(name)}`),
  createPatient: (data: any) => api.post('/admin/patients', data),
  updatePatient: (id: number, data: any) => api.put(`/admin/patients/${id}`, data),
  assignDoctor: (patientId: number, doctorId: number) =>
    api.put(`/admin/patients/${patientId}/assign-doctor/${doctorId}`),
  deletePatient: (id: number) => api.delete(`/admin/patients/${id}`),

  // ==================== DOCTOR MANAGEMENT ====================
  getDoctors: () => api.get('/admin/doctors'),
  getDoctorById: (id: number) => api.get(`/admin/doctors/${id}`),
  searchDoctors: (name: string) => api.get(`/admin/doctors/search?name=${encodeURIComponent(name)}`),
  createDoctor: (data: any) => api.post('/admin/doctors', data),
  updateDoctor: (id: number, data: any) => api.put(`/admin/doctors/${id}`, data),
  deleteDoctor: (id: number) => api.delete(`/admin/doctors/${id}`),

  // ==================== VISIT MANAGEMENT ====================
  getVisits: () => api.get('/admin/visits'),
  getVisitById: (id: number) => api.get(`/admin/visits/${id}`),
  getVisitsByDateRange: (start: string, end: string) =>
    api.get(`/admin/visits/range?start=${start}&end=${end}`),
  deleteVisit: (id: number) => api.delete(`/admin/visits/${id}`),

  // ==================== PRESCRIPTION MANAGEMENT ====================
  getPrescriptions: () => api.get('/admin/prescriptions'),
  getPrescriptionById: (id: number) => api.get(`/admin/prescriptions/${id}`),
  deletePrescription: (id: number) => api.delete(`/admin/prescriptions/${id}`),

  // ==================== LAB ORDER MANAGEMENT ====================
  getLabOrders: () => api.get('/admin/lab-orders'),
  getLabOrderById: (id: number) => api.get(`/admin/lab-orders/${id}`),
  getLabOrdersByStatus: (status: string) => api.get(`/admin/lab-orders/status/${status}`),
  deleteLabOrder: (id: number) => api.delete(`/admin/lab-orders/${id}`),

  // ==================== APPOINTMENT MANAGEMENT ====================
  getAppointments: () => api.get('/admin/appointments'),
  getAppointmentById: (id: number) => api.get(`/admin/appointments/${id}`),
  cancelAppointment: (id: number) => api.put(`/admin/appointments/${id}/cancel`),
  deleteAppointment: (id: number) => api.delete(`/admin/appointments/${id}`),

  // ==================== PHARMACY MANAGEMENT ====================
  getPharmacies: () => api.get('/admin/pharmacies'),
  getPharmacyById: (id: number) => api.get(`/admin/pharmacies/${id}`),
  searchPharmacies: (name: string) => api.get(`/admin/pharmacies/search?name=${encodeURIComponent(name)}`),
  createPharmacy: (data: any) => api.post('/admin/pharmacies', data),
  updatePharmacy: (id: number, data: any) => api.put(`/admin/pharmacies/${id}`, data),
  deletePharmacy: (id: number) => api.delete(`/admin/pharmacies/${id}`),
};
