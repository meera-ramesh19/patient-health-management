# LabService — Project Journal

A chronological record of every conversation, decision, command, and fix made while building LabService.

---

## Session 1: React/TypeScript Frontend (Phase 3)

### User Request
> "add the frontend feature using react/typescript"

### What Was Built

**Project scaffolding:**
```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install react-router-dom axios
```

**API Layer (4 files created):**

`src/api/client.ts` — Axios HTTP client with JWT interceptor:
- Every request automatically adds `Authorization: Bearer <token>` header from localStorage
- 401 responses automatically clear the token and redirect to login
- Base URL set to `/api` which Vite proxies to `localhost:8080`

`src/api/auth.ts` — Authentication API calls:
```typescript
export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post('/auth/register', data),
  forgotPassword: (data: ForgotPasswordRequest) => api.post<string>('/auth/forgot-password', data),
  resetPassword: (data: ResetPasswordRequest) => api.post<string>('/auth/reset-password', data),
};
```

`src/api/patient.ts` — 12 patient portal API methods
`src/api/doctor.ts` — 14 doctor portal API methods
`src/api/admin.ts` — 12 admin portal API methods (later rebuilt to 41)

**Auth Context (`src/context/AuthContext.tsx`):**
- React Context providing global auth state
- `login()`, `register()`, `logout()` functions
- `isAuthenticated` boolean
- `user` object with token, username, role
- Persists to localStorage so login survives page refresh

**Protected Route (`src/components/ProtectedRoute.tsx`):**
- Wrapper component that checks `isAuthenticated` and `user.role`
- Redirects to `/login` if not authenticated
- Redirects to `/login` if role doesn't match `requiredRole` prop

**Navbar (`src/components/Navbar.tsx`):**
- Shows different links based on user role
- Patient: Dashboard, Appointments, Visits, Prescriptions, Lab Orders, Profile
- Doctor: Dashboard, Patients, Appointments, Visits, Prescriptions, Lab Orders, Profile
- Admin: Dashboard, Users, Patients, Doctors, Appointments, Visits, Prescriptions, Lab Orders, Pharmacies
- Shows username and role badge when logged in
- Logout button clears token and redirects

**Auth Pages (4 files):**
- `LoginPage.tsx` — username/password form with role-based redirect after login
- `RegisterPage.tsx` — registration form with portal type selector (patient/doctor)
- `ForgotPasswordPage.tsx` — email input, calls forgot-password endpoint
- `ResetPasswordPage.tsx` — reads token from URL query param, new password form

**Patient Pages (6 files):**
- `PatientDashboard.tsx` — overview cards (upcoming appointments, active prescriptions, pending labs)
- `PatientAppointments.tsx` — view appointments, book new, cancel existing
- `PatientVisits.tsx` — read-only list of past visits
- `PatientPrescriptions.tsx` — list prescriptions with "All" / "Active Only" filter
- `PatientLabOrders.tsx` — list lab orders with status filter buttons
- `PatientProfile.tsx` — view and edit personal info

**Doctor Pages (6 files):**
- `DoctorDashboard.tsx` — today's appointments, patient count, pending labs
- `DoctorPatients.tsx` — list of assigned patients
- `DoctorAppointments.tsx` — view and manage appointment schedule
- `DoctorVisits.tsx` — list visits, create new visits for patients
- `DoctorPrescriptions.tsx` — list and create prescriptions
- `DoctorLabOrders.tsx` — list and create lab orders

**Admin Pages (5 files initially):**
- `AdminDashboard.tsx` — 10 metric cards (total users, patients, doctors, etc.)
- `AdminUsers.tsx` — list users, enable/disable/delete
- `AdminPatients.tsx` — list patients with basic actions
- `AdminDoctors.tsx` — list doctors with basic actions
- `AdminAppointments.tsx` — list all appointments

**App Router (`src/App.tsx`):**
- 4 public routes (login, register, forgot-password, reset-password)
- 7 patient routes (all wrapped in `<ProtectedRoute requiredRole="ROLE_PATIENT">`)
- 7 doctor routes (all wrapped in `<ProtectedRoute requiredRole="ROLE_DOCTOR">`)
- 9 admin routes (all wrapped in `<ProtectedRoute requiredRole="ROLE_ADMIN">`)
- Default `/` redirects to `/login`

**Styling (`src/index.css`):**
- Complete design system with CSS variables
- Navbar, auth pages, forms, buttons, dashboard cards, data tables, status badges
- Responsive breakpoint at 768px

**Vite Config (`vite.config.ts`):**
```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

**Entry Point (`src/main.tsx`):**
- Wraps app in `<BrowserRouter>` and `<AuthProvider>`

### Error Encountered: verbatimModuleSyntax
The tsconfig has `verbatimModuleSyntax: true` and `erasableSyntaxOnly: true`. Importing TypeScript interfaces as values caused build errors.

**Fix:** Separate value imports from type imports:
```typescript
// BEFORE (broken):
import { authApi, LoginRequest, RegisterRequest, LoginResponse } from '../api/auth';

// AFTER (fixed):
import { authApi } from '../api/auth';
import type { LoginRequest, RegisterRequest, LoginResponse } from '../api/auth';
```

---

## Session 2: Google Social Login + JWT Integration

### User Request
> "also integrate the role base access logic with google social logins and jwt tokens logic used in backend"

### What Was Built

**Backend files read for reference (not modified):**
- `AuthController.java` — 6 public endpoints at `/api/auth/**`
- `AuthService.java` — register, login, googleLogin, socialLogin, forgotPassword, resetPassword
- `SocialAuthService.java` — verifies Google/Facebook/GitHub tokens
- `GoogleLoginRequest.java` — `{ googleToken, portalType }`
- `SocialLoginRequest.java` — `{ provider, accessToken, portalType }`

**API Layer Updated:**

`src/api/auth.ts` — added Google and social login methods:
```typescript
export interface GoogleLoginRequest {
  googleToken: string;
  portalType: string;
}

export interface SocialLoginRequest {
  provider: 'google' | 'facebook' | 'github';
  accessToken: string;
  portalType: string;
}

export const authApi = {
  // ... existing methods ...
  googleLogin: (data: GoogleLoginRequest) => api.post<LoginResponse>('/auth/google', data),
  socialLogin: (data: SocialLoginRequest) => api.post<LoginResponse>('/auth/social', data),
};
```

**Auth Context Updated:**
- Added `googleLogin` and `socialLogin` to `AuthContextType` interface
- Added `saveUserData` helper function shared by all login methods
- All three login paths (regular, Google, social) use the same token storage flow

**LoginPage.tsx Updated — Google Sign-In button:**
```typescript
declare global {
  interface Window {
    google?: {
      accounts: { id: {
        initialize: (config: {
          client_id: string;
          callback: (response: { credential: string }) => void;
        }) => void;
        renderButton: (element: HTMLElement, config: {
          theme: string; size: string; width: number; text: string
        }) => void;
      }; };
    };
  }
}
```

- Google Identity Services library loaded via `<script>` tag in `index.html`
- `handleGoogleCallback` sends the credential to `POST /api/auth/google`
- `useEffect` initializes and renders the Google button on mount
- Auth divider ("— or —") between Google and regular login
- `redirectByRole` helper routes user to correct portal after login

**RegisterPage.tsx Updated:**
- Added Google Sign-Up button
- Portal type selector moved above Google button so it applies to both paths

**index.html Updated:**
```html
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

**frontend/.env Created:**
```
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID_HERE
```

**CSS Updated (`index.css`):**
- `.social-login-section` — flex container for social buttons
- `.google-btn-container` — centered Google button wrapper
- `.auth-divider` — "— or —" line between login methods

### How the Google Login Flow Works
```
1. User clicks "Sign in with Google" button
2. Google popup appears → user picks their Google account
3. Google returns a JWT credential (ID token) to the frontend
4. Frontend sends credential to POST /api/auth/google
5. Backend verifies the token with Google's servers
6. Backend finds or creates a user in the database
7. Backend generates OUR JWT token with username + role
8. Frontend stores our token in localStorage
9. Frontend redirects to the correct portal based on role
```

---

## Session 3: 100% Endpoint Coverage (Gap Fill)

### User Request
> "is this the total app in the front end that shows can access and display all the backend points including the dashboard and user, doctor and all the connected portals"

After auditing, the answer was no — several endpoints were missing.

> "yes fill all the gaps"

### Systematic Audit

Read all three portal controllers to count every endpoint:
- `PatientPortalController.java` — 12 endpoints
- `DoctorPortalController.java` — 14 endpoints
- `AdminPortalController.java` — 41 endpoints
- `AuthController.java` — 6 endpoints
- **Total: 73 endpoints**

### Gaps Found and Filled

**Patient portal (3 gaps):**
- `PatientDoctor.tsx` — NEW page showing assigned primary doctor info
- `PatientPrescriptions.tsx` — added "Active Only" filter button
- `PatientLabOrders.tsx` — added status filter buttons (ALL/ORDERED/IN_PROGRESS/COMPLETED/CANCELLED)
- `patient.ts` API — added `getLabOrdersByStatus()` method

**Doctor portal (3 gaps):**
- `DoctorProfile.tsx` — NEW page for viewing/editing doctor profile
- `DoctorVisits.tsx` — added inline editing (Edit → edit diagnosis/notes → Save/Cancel)
- `doctor.ts` API — added `updateVisit()` method

**Admin portal (32 gaps — massive rebuild):**

`admin.ts` API rebuilt from 12 to 41 methods:
```typescript
export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),

  // Users: 6 methods
  getUsers: () => api.get('/admin/users'),
  getUserById: (id: number) => api.get(`/admin/users/${id}`),
  enableUser: (id: number) => api.put(`/admin/users/${id}/enable`),
  disableUser: (id: number) => api.put(`/admin/users/${id}/disable`),
  changeUserRole: (id: number, role: string) =>
    api.put(`/admin/users/${id}/role`, JSON.stringify(role),
      { headers: { 'Content-Type': 'application/json' } }),
  deleteUser: (id: number) => api.delete(`/admin/users/${id}`),

  // Patients: 7 methods (get, getById, search, create, update, assignDoctor, delete)
  // Doctors: 6 methods (get, getById, search, create, update, delete)
  // Visits: 4 methods (get, getById, getByDateRange, delete)
  // Prescriptions: 3 methods (get, getById, delete)
  // Lab Orders: 4 methods (get, getById, getByStatus, delete)
  // Appointments: 4 methods (get, getById, cancel, delete)
  // Pharmacies: 6 methods (get, getById, search, create, update, delete)
  // ... (41 total)
};
```

Admin pages rebuilt/created:
- `AdminUsers.tsx` — REBUILT: added "Role" button with prompt for role change
- `AdminPatients.tsx` — REBUILT: full CRUD with search, create form, inline edit, assign doctor, delete
- `AdminDoctors.tsx` — REBUILT: full CRUD with search, create, inline edit, delete
- `AdminAppointments.tsx` — REBUILT: added Cancel and Delete buttons
- `AdminVisits.tsx` — NEW: view all visits, date range filter, delete
- `AdminPrescriptions.tsx` — NEW: view all prescriptions, delete
- `AdminLabOrders.tsx` — NEW: view all lab orders, status filter, delete
- `AdminPharmacies.tsx` — NEW: full CRUD (create/edit/delete/search)

**Routes and navigation updated:**
- `App.tsx` — added 6 new routes
- `Navbar.tsx` — added missing links for all portals

**CSS updated (`index.css`):**
- `.search-bar` — search input styling for admin pages
- `.filter-group` — toggle button group for status filters
- `.inline-input` — input fields for inline editing

### Final Coverage
```
Portal          Backend Endpoints    Frontend Coverage
──────          ─────────────────    ─────────────────
Auth            6                    6/6   (100%)
Patient         12                   12/12 (100%)
Doctor          14                   14/14 (100%)
Admin           41                   41/41 (100%)
──────          ──                   ─────
TOTAL           73                   73/73 (100%)
```

---

## Session 4: End-to-End Verification

### User Request
> "so can i run this as a full crud app now"

### Verification Steps

**1. Frontend build — SUCCESS:**
```bash
cd /Users/meeraramesh/LabService/frontend
npx vite build
# → 109 modules transformed, 0 errors
```

**2. Backend compile — SUCCESS:**
```bash
cd /Users/meeraramesh/LabService
mvn compile
# → BUILD SUCCESS
```

**3. Maven wrapper was missing:**
```bash
./mvnw
# → command not found

mvn wrapper:wrapper
# → Generated mvnw and mvnw.cmd
```

**4. Backend tests — SUCCESS:**
```bash
./mvnw test
# → 31 tests, 0 failures, BUILD SUCCESS
```

**5. Backend startup — CRASHED:**
```bash
./mvnw spring-boot:run
# → Failed to execute SQL script statement #1 of file [data.sql]:
#   INSERT INTO users (username, password, email, role, enabled) VALUES...
```

### Bug #1: data.sql Runs Before Tables Exist

**Root cause:** Spring Boot runs `data.sql` BEFORE Hibernate's `ddl-auto=create-drop` creates the tables.

**Fix:** Added to `application.properties`:
```properties
spring.jpa.defer-datasource-initialization=true
```
This tells Spring: "Wait for Hibernate to create all tables, THEN run data.sql."

### Bug #2: Wrong BCrypt Hash in data.sql

After fixing the startup order, login returned 500 "Bad credentials". The bcrypt hash in `data.sql` didn't actually match "admin123".

**Diagnosis:**
```python
import bcrypt
existing = b'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
for pw in [b'admin123', b'password', b'password123', b'admin']:
    if bcrypt.checkpw(pw, existing):
        print(f'Match: {pw.decode()}')
# → No common password matched
```

**Fix:** Generated correct hash and updated `data.sql`:
```python
new_hash = bcrypt.hashpw(b'admin123', bcrypt.gensalt(rounds=10)).decode()
# → $2b$10$HE0WP8RkKjnagL02/CDvFuWIS7aT9b/LIJd2.dqL9QjVDH0KpyPwm
```

### Successful End-to-End Test

**Backend started on port 8080:**
```bash
./mvnw spring-boot:run
# → Started LabServiceApplication in 3.794 seconds
```

**Admin login — SUCCESS:**
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# → {"token":"eyJ...","username":"admin","role":"ROLE_ADMIN"}
```

**Frontend started on port 3000:**
```bash
cd frontend && npx vite --port 3000
```

**Frontend-to-backend proxy — SUCCESS:**
```bash
curl -s -X POST http://localhost:3000/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# → {"token":"eyJ...","username":"admin","role":"ROLE_ADMIN"}
```

---

## Session 5: PostgreSQL Database Connection (Current Session)

### User Request
> "it also needs a database connection"

At this point the app used H2 (in-memory — data lost on every restart). User wanted a real, persistent database.

### Checking Available Databases

```bash
mysql --version
# → mysql Ver 8.0.38 (installed but not running)

psql --version
# → PostgreSQL 14.19 (Postgres.app)

pg_isready
# → /tmp:5432 - accepting connections (PostgreSQL already running!)
```

PostgreSQL was already running. MySQL needed sudo to start (permission issues with data directory).

### Postgres.app Permission Issue

First attempt to connect failed:
```bash
psql -h localhost -U meeraramesh -d postgres -c "SELECT 1"
# → FATAL: Postgres.app rejected "trust" authentication
# → DETAIL: You did not allow iTerm2 to connect without a password
```

**Fix:** User opened Postgres.app → Settings → allowed iTerm2 in app permissions.

After that:
```bash
psql -h localhost -U meeraramesh -d postgres -c "SELECT 1"
# → 1 row returned ✓
```

### Database Created

```bash
psql -h localhost -U meeraramesh -d postgres -c "CREATE DATABASE labservicedb;"
# → CREATE DATABASE
```

### Files Changed

**1. pom.xml — Added PostgreSQL driver:**
```xml
<!-- Added: PostgreSQL JDBC driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Changed: H2 moved from runtime to test-only -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>  <!-- was: runtime -->
</dependency>
```

**2. application.properties — Database connection:**
```properties
# BEFORE:
spring.datasource.url=jdbc:h2:mem:labservicedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# AFTER:
spring.datasource.url=jdbc:postgresql://localhost:5432/labservicedb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=meeraramesh
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=false

# New properties needed:
spring.sql.init.mode=always
# ↑ Without this, data.sql doesn't run for non-embedded databases

spring.jpa.defer-datasource-initialization=true
# ↑ Run data.sql AFTER Hibernate creates tables
```

**3. data.sql — PostgreSQL-safe insert:**
```sql
-- BEFORE:
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2b$10$...', 'admin@labservice.com', 'ROLE_ADMIN', true);

-- AFTER:
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2b$10$...', 'admin@labservice.com', 'ROLE_ADMIN', true)
ON CONFLICT (username) DO NOTHING;
```

### Troubleshooting the Migration

**Problem 1: data.sql not executing**
First restart with PostgreSQL — login returned "Bad credentials". Checked the database:
```bash
psql -h localhost -U meeraramesh -d labservicedb -c "SELECT * FROM users;"
# → (0 rows)
```
The admin user was never inserted. Root cause: `spring.sql.init.mode` defaults to `embedded` which only runs `data.sql` for H2/HSQLDB.

**Fix:** Added `spring.sql.init.mode=always`.

**Problem 2: Hibernate dialect warning**
```
HHH90000025: PostgreSQLDialect does not need to be specified explicitly
```
Initially added `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect` but Hibernate auto-detects it. Removed the explicit setting.

### Successful PostgreSQL Connection

```bash
./mvnw spring-boot:run
# → HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection
# → Started LabServiceApplication in 4.397 seconds

curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# → {"token":"eyJ...","username":"admin","role":"ROLE_ADMIN"}

psql -h localhost -U meeraramesh -d labservicedb \
  -c "SELECT id, username, role FROM users;"
# → 1 | admin | ROLE_ADMIN
```

### Documentation Updated

Updated both `PLAN.md` and `LEARNING_LOG.md`:
- Tools & Technologies table — PostgreSQL as primary database
- Maven dependencies — PostgreSQL driver + H2 test scope
- application.properties section — full PostgreSQL config
- data.sql section — ON CONFLICT DO NOTHING
- Quick Start commands — PostgreSQL prerequisites, psql commands
- New Phase 4 section in LEARNING_LOG with full before/after comparisons

### Servers Stopped

```bash
# Killed frontend (port 3000):
kill 36977

# Killed backend (port 8080):
pkill -f 'com.labservice.LabServiceApplication'
pkill -f 'spring-boot:run'
```

---

## Final State of the Application

### Architecture
```
Browser (React 19 + TypeScript)
    ↓ axios + JWT Bearer token
Vite Dev Server (localhost:3000)
    ↓ proxy /api →
Spring Boot 3.4.4 (localhost:8080)
    ↓ Spring Security + JWT authentication
    ↓ Role-based access control (PATIENT / DOCTOR / ADMIN)
    ↓ Hibernate 6.6 ORM
PostgreSQL 14 (localhost:5432/labservicedb)
    ↓ persistent storage on disk
```

### File Count
```
Backend:  62 source files + 4 test files = 66 Java files
Frontend: 34 files (components, pages, API layer, context, CSS)
Config:   application.properties, data.sql, pom.xml, vite.config.ts, tsconfig.json
Docs:     PLAN.md, LEARNING_LOG.md, PROJECT_JOURNAL.md
```

### How to Run
```bash
# 1. Make sure PostgreSQL is running:
pg_isready
# → accepting connections

# 2. Start backend:
cd /Users/meeraramesh/LabService
./mvnw spring-boot:run
# → http://localhost:8080

# 3. Start frontend (separate terminal):
cd /Users/meeraramesh/LabService/frontend
npx vite --port 3000
# → http://localhost:3000

# 4. Login with seeded admin:
#    Username: admin
#    Password: admin123
```

### What's Working
- Full authentication (register, login, JWT tokens, role-based guards)
- 3 portals with separate dashboards and permissions
- 73/73 backend endpoints covered by frontend (100%)
- Full CRUD operations across all entities
- Persistent PostgreSQL database (data survives restarts)
- Seeded admin account for immediate access

### What Needs Real Credentials
- Google Sign-In — needs Google Cloud Client ID in `frontend/.env` and `application.properties`
- Password reset emails — needs Gmail SMTP credentials in `application.properties`
