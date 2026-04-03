import api from './client';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  portalType: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

// Google Sign-In — sends Google's ID token to our backend for verification
// Backend: POST /api/auth/google → { googleToken, portalType }
// portalType is only needed on FIRST login (when auto-creating account)
export interface GoogleLoginRequest {
  googleToken: string;
  portalType: string;
}

// Unified social login — works with Google, Facebook, GitHub
// Backend: POST /api/auth/social → { provider, accessToken, portalType }
export interface SocialLoginRequest {
  provider: 'google' | 'facebook' | 'github';
  accessToken: string;
  portalType: string;
}

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    api.post('/auth/register', data),

  // Google-specific login (uses Google ID token)
  googleLogin: (data: GoogleLoginRequest) =>
    api.post<LoginResponse>('/auth/google', data),

  // Unified social login (Google, Facebook, GitHub)
  socialLogin: (data: SocialLoginRequest) =>
    api.post<LoginResponse>('/auth/social', data),

  forgotPassword: (data: ForgotPasswordRequest) =>
    api.post<string>('/auth/forgot-password', data),

  resetPassword: (data: ResetPasswordRequest) =>
    api.post<string>('/auth/reset-password', data),
};
