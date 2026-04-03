import api from './client';

export const authApi = {
  login: (data) =>
    api.post('/auth/login', data),

  register: (data) =>
    api.post('/auth/register', data),

  googleLogin: (data) =>
    api.post('/auth/google', data),

  socialLogin: (data) =>
    api.post('/auth/social', data),

  forgotPassword: (data) =>
    api.post('/auth/forgot-password', data),

  resetPassword: (data) =>
    api.post('/auth/reset-password', data),
};
