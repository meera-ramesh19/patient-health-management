# LabService — Complete Java Project Build Plan

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Portal Architecture](#2-portal-architecture-three-portals)
3. [Tools and Technologies](#3-tools-and-technologies)
4. [Project Folder Structure](#4-project-folder-structure-and-what-each-folder-does)
5. [The 4-Layer Architecture](#5-the-4-layer-architecture-how-components-work-together)
6. [Data Models and Relationships](#6-data-models-and-entity-relationships)
7. [Role-Based Access Control (RBAC)](#7-role-based-access-control-rbac)
8. [Component Deep Dive](#8-component-deep-dive-with-code-examples)
9. [Build Phases](#9-build-phases-step-by-step)
10. [API Endpoints](#10-api-endpoints-full-list)
11. [Key Java and Spring Concepts](#11-key-java-and-spring-concepts-glossary)

---

## 1. Project Overview

LabService is a Spring Boot web application for healthcare management with **three separate portals**:

- **Patient Portal** — Patients log in to view visits, prescriptions, lab results, and book appointments
- **Doctor Portal** — Doctors log in to manage patients, record visits, write prescriptions, and order labs
- **Admin Portal** — Admins manage all users, doctors, patients, system settings, and view reports

Each portal has its own login page, dashboard, and set of features based on the user's role. The backend uses **Role-Based Access Control (RBAC)** to enforce who can access what.

The app exposes a REST API (JSON over HTTP) that a frontend (web or mobile) can consume.

---

## 2. Portal Architecture (Three Portals)

### How the Three Portals Work

```
                        ┌─────────────────────┐
                        │    LOGIN PAGE        │
                        │                      │
                        │  Username: [______]  │
                        │  Password: [______]  │
                        │  Portal:   [v    ]   │
                        │    ○ Patient          │
                        │    ○ Doctor           │
                        │    ○ Admin            │
                        │                      │
                        │  [    Login    ]      │
                        └─────────┬───────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
                    ▼             ▼             ▼
    ┌───────────────────┐ ┌──────────────────┐ ┌───────────────────┐
    │  PATIENT PORTAL   │ │  DOCTOR PORTAL   │ │  ADMIN PORTAL     │
    │  /patient/**      │ │  /doctor/**      │ │  /admin/**        │
    ├───────────────────┤ ├──────────────────┤ ├───────────────────┤
    │ • My Dashboard    │ │ • My Dashboard   │ │ • System Dashboard│
    │ • My Visits       │ │ • My Patients    │ │ • Manage Users    │
    │ • My Prescriptions│ │ • Record Visit   │ │ • Manage Doctors  │
    │ • My Lab Results  │ │ • Write Rx       │ │ • Manage Patients │
    │ • My Appointments │ │ • Order Labs     │ │ • View All Visits │
    │ • My Doctors      │ │ • My Schedule    │ │ • View All Labs   │
    │ • My Profile      │ │ • My Profile     │ │ • View All Appts  │
    │                   │ │                  │ │ • Reports         │
    └───────────────────┘ └──────────────────┘ └───────────────────┘
```

### What Each Portal Can Do

#### Patient Portal (Role: ROLE_PATIENT)

| Feature              | Description                                       | API Prefix           |
|----------------------|---------------------------------------------------|----------------------|
| **Dashboard**        | Overview: upcoming appointments, active Rx, pending labs | GET /api/patient/dashboard |
| **My Visits**        | View past doctor visits (read-only)               | GET /api/patient/visits |
| **My Prescriptions** | View current and past prescriptions               | GET /api/patient/prescriptions |
| **My Lab Results**   | View lab orders and results when available         | GET /api/patient/lab-orders |
| **My Appointments**  | View, book, and cancel future appointments        | /api/patient/appointments |
| **My Doctors**       | View assigned doctors and their info               | GET /api/patient/doctors |
| **My Profile**       | View and update personal info                      | /api/patient/profile |

**Key restriction:** Patients can only see **their own** data. A patient cannot view another patient's visits or prescriptions.

#### Doctor Portal (Role: ROLE_DOCTOR)

| Feature              | Description                                       | API Prefix           |
|----------------------|---------------------------------------------------|----------------------|
| **Dashboard**        | Today's appointments, pending labs, patient count  | GET /api/doctor/dashboard |
| **My Patients**      | View all assigned patients                         | GET /api/doctor/patients |
| **Record Visit**     | Create visit records with diagnosis and notes      | POST /api/doctor/visits |
| **Write Prescription** | Prescribe medications during a visit            | POST /api/doctor/prescriptions |
| **Order Labs**       | Order lab tests and view results                   | /api/doctor/lab-orders |
| **My Schedule**      | View and manage appointment calendar               | /api/doctor/appointments |
| **My Profile**       | Update profile, address, and specialty              | /api/doctor/profile |

**Key restriction:** Doctors can only see **their own patients'** data and **their own schedule**. They cannot modify another doctor's visits.

#### Admin Portal (Role: ROLE_ADMIN)

| Feature              | Description                                       | API Prefix           |
|----------------------|---------------------------------------------------|----------------------|
| **Dashboard**        | System-wide stats: total users, visits, labs       | GET /api/admin/dashboard |
| **Manage Users**     | Create, update, disable, delete user accounts      | /api/admin/users |
| **Manage Doctors**   | Add new doctors, update info, deactivate           | /api/admin/doctors |
| **Manage Patients**  | View all patients, reassign doctors                | /api/admin/patients |
| **View All Visits**  | Search and view any visit record                   | /api/admin/visits |
| **View All Labs**    | View all lab orders across the system              | /api/admin/lab-orders |
| **View All Appointments** | View and manage all appointments              | /api/admin/appointments |
| **Reports**          | Generate usage reports and statistics              | /api/admin/reports |

**Key privilege:** Admins can see and manage **everything**. They are the only ones who can create/delete user accounts and assign roles.

### How Roles Are Assigned

```
1. User registers at /api/auth/register
   → Selects portal type: "patient" or "doctor"
   → Account is created with role ROLE_PATIENT or ROLE_DOCTOR
   → Doctor accounts may require admin approval before activation

2. Admin creates accounts at /api/admin/users
   → Can assign any role: ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN
   → Can change a user's role
   → Can enable/disable accounts

3. On login, the JWT token contains the user's role
   → Frontend reads the role and redirects to the correct portal
   → Backend validates the role on every API request
```

### Portal URL Structure

```
/api/auth/**              — Public (no login required)
    /api/auth/register        — Create account
    /api/auth/login           — Log in, receive JWT token
    /api/auth/logout          — Log out

/api/patient/**           — ROLE_PATIENT only
    /api/patient/dashboard
    /api/patient/visits
    /api/patient/prescriptions
    /api/patient/lab-orders
    /api/patient/appointments
    /api/patient/doctors
    /api/patient/profile

/api/doctor/**            — ROLE_DOCTOR only
    /api/doctor/dashboard
    /api/doctor/patients
    /api/doctor/visits
    /api/doctor/prescriptions
    /api/doctor/lab-orders
    /api/doctor/appointments
    /api/doctor/profile

/api/admin/**             — ROLE_ADMIN only
    /api/admin/dashboard
    /api/admin/users
    /api/admin/doctors
    /api/admin/patients
    /api/admin/visits
    /api/admin/lab-orders
    /api/admin/appointments
    /api/admin/reports
```

---

## 3. Tools and Technologies

| Tool               | Purpose                                         | Why We Use It                                      |
|---------------------|------------------------------------------------|---------------------------------------------------|
| **JDK 17+**        | Java Development Kit                            | The language itself — compiles and runs Java code  |
| **Maven**          | Build tool and dependency manager               | Downloads libraries, compiles code, runs tests     |
| **Spring Boot**    | Application framework                           | Auto-configures everything, embedded web server    |
| **Spring Web**     | REST API support                                | Provides @RestController, @GetMapping, etc.        |
| **Spring Data JPA**| Database access layer                           | Auto-generates SQL from method names               |
| **Hibernate**      | ORM (Object-Relational Mapping)                 | Maps Java classes to database tables               |
| **PostgreSQL**     | Persistent database                             | Data survives restarts, production-ready           |
| **H2 Database**    | In-memory database for testing                  | Used in unit tests only                            |
| **Spring Security**| Authentication and authorization                | Handles login, passwords, session management       |
| **Lombok** (optional)| Reduces boilerplate code                     | Auto-generates getters, setters, constructors      |
| **Postman**        | API testing tool                                | Send requests to your API and inspect responses    |
| **IntelliJ IDEA / VS Code** | Code editor                          | Write and debug Java code                          |

### Maven Dependencies (pom.xml)

These are the libraries your project needs. Maven downloads them automatically:

```xml
<dependencies>
    <!-- Spring Boot core — auto-configuration, embedded Tomcat server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA — repository pattern, database access -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL — persistent database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- H2 — in-memory database for testing only -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Security — login, password hashing, access control -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Validation — @NotNull, @Email, @Size annotations -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Testing — JUnit, MockMvc -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 4. Project Folder Structure (and What Each Folder Does)

```
LabService/
├── pom.xml                              # BUILD FILE — lists all dependencies (libraries)
│                                        #   Maven reads this to know what to download
│                                        #   Also defines Java version, project name, plugins
│
├── src/
│   ├── main/
│   │   ├── java/com/labservice/
│   │   │   │
│   │   │   ├── LabServiceApplication.java
│   │   │   │   # ENTRY POINT — the main() method lives here
│   │   │   │   # Spring Boot starts the embedded Tomcat server
│   │   │   │   # Scans all sub-packages for @Component, @Service, etc.
│   │   │   │
│   │   │   ├── model/                   # MODELS (Entities)
│   │   │   │   │                        # Define WHAT your data looks like
│   │   │   │   │                        # Each class = one database table
│   │   │   │   │                        # Fields = columns in that table
│   │   │   │   │                        # Annotations define relationships between tables
│   │   │   │   │
│   │   │   │   ├── User.java           # Login credentials + role (PATIENT/DOCTOR/ADMIN)
│   │   │   │   ├── Role.java           # Enum: ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN
│   │   │   │   ├── Patient.java        # Patient profile (name, DOB, contact info)
│   │   │   │   ├── Doctor.java         # Doctor profile (name, specialty, license)
│   │   │   │   ├── Address.java        # Physical address + map coordinates (lat/lng)
│   │   │   │   ├── Visit.java          # A record of a doctor visit (date, notes, diagnosis)
│   │   │   │   ├── Prescription.java   # Medication prescribed during a visit
│   │   │   │   ├── LabOrder.java       # Lab test ordered for a patient (type, status, results)
│   │   │   │   └── Appointment.java    # Future scheduled appointment
│   │   │   │
│   │   │   ├── repository/             # REPOSITORIES (Data Access)
│   │   │   │   │                       # Define HOW you read/write data to the database
│   │   │   │   │                       # Each repository is an interface (not a class)
│   │   │   │   │                       # Spring auto-generates the implementation
│   │   │   │   │                       # You get CRUD methods for free (save, find, delete)
│   │   │   │   │
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── PatientRepository.java
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   ├── VisitRepository.java
│   │   │   │   ├── PrescriptionRepository.java
│   │   │   │   ├── LabOrderRepository.java
│   │   │   │   └── AppointmentRepository.java
│   │   │   │
│   │   │   ├── service/                # SERVICES (Business Logic)
│   │   │   │   │                       # Define the RULES and DECISIONS of your app
│   │   │   │   │                       # Shared services used by all portals
│   │   │   │   │
│   │   │   │   ├── AuthService.java            # Login, register, JWT token generation
│   │   │   │   ├── UserService.java            # User account management
│   │   │   │   ├── PatientService.java         # Patient CRUD + business rules
│   │   │   │   ├── DoctorService.java          # Doctor CRUD + business rules
│   │   │   │   ├── VisitService.java           # Visit recording logic
│   │   │   │   ├── PrescriptionService.java    # Prescription management
│   │   │   │   ├── LabOrderService.java        # Lab order lifecycle
│   │   │   │   ├── AppointmentService.java     # Booking + conflict detection
│   │   │   │   └── DashboardService.java       # Dashboard stats for each portal
│   │   │   │
│   │   │   ├── controller/             # CONTROLLERS (API Endpoints)
│   │   │   │   │                       # Organized by PORTAL — each portal gets its own
│   │   │   │   │                       # sub-package with dedicated controllers
│   │   │   │   │
│   │   │   │   ├── auth/                       # PUBLIC — no login required
│   │   │   │   │   └── AuthController.java     #   POST /api/auth/register
│   │   │   │   │                               #   POST /api/auth/login
│   │   │   │   │
│   │   │   │   ├── patient/                    # PATIENT PORTAL — ROLE_PATIENT only
│   │   │   │   │   ├── PatientDashboardController.java    # GET /api/patient/dashboard
│   │   │   │   │   ├── PatientVisitController.java        # GET /api/patient/visits
│   │   │   │   │   ├── PatientPrescriptionController.java # GET /api/patient/prescriptions
│   │   │   │   │   ├── PatientLabOrderController.java     # GET /api/patient/lab-orders
│   │   │   │   │   ├── PatientAppointmentController.java  # /api/patient/appointments
│   │   │   │   │   └── PatientProfileController.java      # /api/patient/profile
│   │   │   │   │
│   │   │   │   ├── doctor/                     # DOCTOR PORTAL — ROLE_DOCTOR only
│   │   │   │   │   ├── DoctorDashboardController.java     # GET /api/doctor/dashboard
│   │   │   │   │   ├── DoctorPatientController.java       # GET /api/doctor/patients
│   │   │   │   │   ├── DoctorVisitController.java         # /api/doctor/visits
│   │   │   │   │   ├── DoctorPrescriptionController.java  # /api/doctor/prescriptions
│   │   │   │   │   ├── DoctorLabOrderController.java      # /api/doctor/lab-orders
│   │   │   │   │   ├── DoctorAppointmentController.java   # /api/doctor/appointments
│   │   │   │   │   └── DoctorProfileController.java       # /api/doctor/profile
│   │   │   │   │
│   │   │   │   └── admin/                      # ADMIN PORTAL — ROLE_ADMIN only
│   │   │   │       ├── AdminDashboardController.java      # GET /api/admin/dashboard
│   │   │   │       ├── AdminUserController.java           # /api/admin/users
│   │   │   │       ├── AdminDoctorController.java         # /api/admin/doctors
│   │   │   │       ├── AdminPatientController.java        # /api/admin/patients
│   │   │   │       ├── AdminVisitController.java          # /api/admin/visits
│   │   │   │       ├── AdminLabOrderController.java       # /api/admin/lab-orders
│   │   │   │       ├── AdminAppointmentController.java    # /api/admin/appointments
│   │   │   │       └── AdminReportController.java         # /api/admin/reports
│   │   │   │
│   │   │   ├── dto/                    # DTOs (Data Transfer Objects)
│   │   │   │   │                       # Shape the data sent TO and FROM the API
│   │   │   │   │                       # Different portals may get different response shapes
│   │   │   │   │
│   │   │   │   ├── request/            # What the client sends TO the API
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java        # Includes portalType field
│   │   │   │   │   ├── CreatePatientRequest.java
│   │   │   │   │   ├── CreateDoctorRequest.java
│   │   │   │   │   ├── CreateVisitRequest.java
│   │   │   │   │   ├── CreatePrescriptionRequest.java
│   │   │   │   │   ├── CreateLabOrderRequest.java
│   │   │   │   │   ├── CreateAppointmentRequest.java
│   │   │   │   │   └── UpdateUserRoleRequest.java  # Admin only
│   │   │   │   │
│   │   │   │   └── response/           # What the API sends BACK to the client
│   │   │   │       ├── LoginResponse.java          # Includes role + JWT token
│   │   │   │       ├── PatientDashboardResponse.java
│   │   │   │       ├── DoctorDashboardResponse.java
│   │   │   │       ├── AdminDashboardResponse.java
│   │   │   │       ├── PatientResponse.java
│   │   │   │       ├── DoctorResponse.java
│   │   │   │       └── ApiErrorResponse.java
│   │   │   │
│   │   │   ├── security/               # SECURITY (Authentication + Authorization)
│   │   │   │   │                       # JWT-based auth with role enforcement
│   │   │   │   │
│   │   │   │   ├── SecurityConfig.java         # URL-to-role mapping rules
│   │   │   │   ├── JwtTokenProvider.java       # Generate + validate JWT tokens
│   │   │   │   ├── JwtAuthenticationFilter.java # Intercepts every request, checks token
│   │   │   │   └── CustomUserDetailsService.java # Loads user + role from database
│   │   │   │
│   │   │   └── exception/              # EXCEPTION HANDLING
│   │   │       │                       # Custom error classes + global error handler
│   │   │       │
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── BadRequestException.java
│   │   │       ├── UnauthorizedException.java      # 401 — not logged in
│   │   │       ├── ForbiddenException.java          # 403 — wrong role
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties  # APP SETTINGS
│   │       │                           #   Database URL, port, JWT secret, logging level
│   │       │                           #   Spring reads this on startup
│   │       │
│   │       ├── data.sql                # SEED DATA — creates default admin account on startup
│   │       │
│   │       ├── static/                 # STATIC FILES (CSS, JS, images)
│   │       │                           #   Served directly to the browser
│   │       │
│   │       └── templates/              # HTML TEMPLATES (if using Thymeleaf)
│   │                                   #   Server-rendered pages
│   │
│   └── test/                           # TESTS
│       └── java/com/labservice/        #   Mirrors the main source structure
│           ├── controller/
│           │   ├── patient/            #   Test patient portal endpoints
│           │   ├── doctor/             #   Test doctor portal endpoints
│           │   └── admin/              #   Test admin portal endpoints
│           ├── service/                #   Test business logic with mocks
│           └── repository/             #   Test database queries
```

---

## 5. The 4-Layer Architecture (How Components Work Together)

### The Restaurant Analogy

Think of your application like a restaurant:

```
CUSTOMER (Browser / Mobile App / Postman)
    │
    │  "I'd like to book an appointment"  (HTTP Request)
    ▼
┌──────────────────────────────────────────────────────────┐
│  CONTROLLER  — The WAITER                                │
│                                                          │
│  Takes orders from the customer (HTTP requests).         │
│  Doesn't cook — just passes the order to the kitchen.    │
│  Returns the finished dish to the customer (response).   │
│                                                          │
│  Example: AppointmentController receives                 │
│    POST /api/appointments with JSON body                 │
│    → calls AppointmentService.bookAppointment()          │
│    → returns the created Appointment as JSON             │
└──────────────────────┬───────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────┐
│  SERVICE  — The CHEF                                     │
│                                                          │
│  Knows the recipes (business rules and logic).           │
│  Makes decisions: "Is this valid? Is this allowed?"      │
│  Combines ingredients from the pantry (repository).      │
│                                                          │
│  Example: AppointmentService checks:                     │
│    - Does the doctor exist?                              │
│    - Is the time slot available?                         │
│    - Is the patient eligible?                            │
│    → If all good, saves the appointment                  │
└──────────────────────┬───────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────┐
│  REPOSITORY  — The PANTRY                                │
│                                                          │
│  Stores and retrieves ingredients (data).                │
│  Doesn't decide what to cook — just provides raw data.   │
│  Talks directly to the database using auto-generated SQL.│
│                                                          │
│  Example: AppointmentRepository                          │
│    .findByDoctorIdAndDateTime(doctorId, dateTime)        │
│    → generates: SELECT * FROM appointments               │
│       WHERE doctor_id = ? AND date_time = ?              │
└──────────────────────┬───────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────┐
│  DATABASE  — The STORAGE ROOM                            │
│                                                          │
│  Where everything is permanently saved.                  │
│  Tables, rows, columns — structured data.                │
│  PostgreSQL for persistent storage. H2 for testing only.  │
└──────────────────────────────────────────────────────────┘
```

### Request Flow (Real Example)

When a user books an appointment, here's exactly what happens:

```
1. User clicks "Book Appointment" button on the website
2. Browser sends: POST /api/appointments
   Body: { "patientId": 1, "doctorId": 5, "dateTime": "2026-04-01T10:00" }

3. AppointmentController.create() receives the request
   → Extracts the JSON into a CreateAppointmentRequest object
   → Calls appointmentService.bookAppointment(request)

4. AppointmentService.bookAppointment() runs business logic
   → Calls appointmentRepository.existsByDoctorIdAndDateTime(5, "2026-04-01T10:00")
   → If true: throws "Time slot already taken" error
   → If false: creates new Appointment object
   → Calls appointmentRepository.save(appointment)

5. AppointmentRepository.save() talks to the database
   → Hibernate generates: INSERT INTO appointments (patient_id, doctor_id, date_time, status)
                           VALUES (1, 5, '2026-04-01 10:00:00', 'SCHEDULED')
   → Database returns the saved row with generated ID

6. Response flows back up:
   → Repository returns saved Appointment to Service
   → Service returns it to Controller
   → Controller converts it to JSON and sends HTTP 201 Created
   → Browser receives: { "id": 42, "patientId": 1, "doctorId": 5, ... }
```

### Why Layers Matter

| Without Layers (Spaghetti)          | With Layers (Clean)                  |
|--------------------------------------|--------------------------------------|
| Database code mixed with HTML        | Each layer has ONE responsibility    |
| Change database = rewrite everything | Change database = only change Repository |
| Can't test business rules alone      | Test each layer independently        |
| One bug breaks everything            | Bugs are isolated to their layer     |
| Hard for teams to work together      | Different people work on different layers |

---

## 6. Data Models and Entity Relationships

### Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐
│    User      │       │   Address    │
├─────────────┤       ├─────────────┤
│ id           │       │ id           │
│ username     │       │ street       │
│ password     │       │ city         │
│ email        │       │ state        │
│ role         │       │ zipCode      │
└──────┬──────┘       │ latitude     │
       │ 1:1          │ longitude    │
       ▼              └──────┬──────┘
┌─────────────┐              │ 1:1
│   Patient    │              │
├─────────────┤       ┌──────┴──────┐
│ id           │       │   Doctor     │
│ firstName    │  N:1  ├─────────────┤
│ lastName     │◄──────│ id           │
│ dateOfBirth  │       │ firstName    │
│ phone        │       │ lastName     │
│ user_id (FK) │       │ specialty    │
│ doctor_id(FK)│       │ phone        │
└──────┬──────┘       │ licenseNumber│
       │               │ address_id   │
       │               └─────────────┘
       │
       ├──── 1:N ────┐
       │              │
       ▼              ▼
┌─────────────┐  ┌──────────────┐
│    Visit     │  │  Appointment  │
├─────────────┤  ├──────────────┤
│ id           │  │ id            │
│ visitDate    │  │ dateTime      │
│ reason       │  │ status        │
│ diagnosis    │  │ reason        │
│ notes        │  │ notes         │
│ patient_id   │  │ patient_id    │
│ doctor_id    │  │ doctor_id     │
└──────┬──────┘  └──────────────┘
       │
       ├──── 1:N ────┐
       │              │
       ▼              ▼
┌──────────────┐ ┌─────────────┐
│ Prescription │ │  LabOrder    │
├──────────────┤ ├─────────────┤
│ id            │ │ id           │
│ medicationName│ │ testName     │
│ dosage        │ │ testCode     │
│ frequency     │ │ status       │
│ startDate     │ │ orderedDate  │
│ endDate       │ │ completedDate│
│ instructions  │ │ results      │
│ visit_id      │ │ notes        │
│ doctor_id     │ │ visit_id     │
└──────────────┘ │ patient_id   │
                  │ doctor_id    │
                  └─────────────┘
```

### Relationship Explanations

| Relationship               | Type     | Meaning                                          |
|----------------------------|----------|--------------------------------------------------|
| User → Patient             | One-to-One   | Each user account has exactly one patient profile |
| Patient → Doctor           | Many-to-One  | Many patients can share one primary doctor        |
| Doctor → Address           | One-to-One   | Each doctor has one practice address              |
| Patient → Visit            | One-to-Many  | One patient can have many visits                  |
| Visit → Prescription       | One-to-Many  | One visit can result in many prescriptions        |
| Visit → LabOrder           | One-to-Many  | One visit can order many lab tests                |
| Patient → Appointment      | One-to-Many  | One patient can have many future appointments     |
| Doctor → Appointment       | One-to-Many  | One doctor can have many appointments             |
| Doctor → Visit             | One-to-Many  | One doctor can see many patients in visits         |

### JPA Annotation Reference for Relationships

```java
// ONE-TO-ONE: User ↔ Patient
// In Patient.java:
@OneToOne
@JoinColumn(name = "user_id")   // Creates a user_id column in patients table
private User user;

// MANY-TO-ONE: Many Patients → One Doctor
// In Patient.java:
@ManyToOne
@JoinColumn(name = "doctor_id") // Creates a doctor_id column in patients table
private Doctor primaryDoctor;

// ONE-TO-MANY: One Patient → Many Visits
// In Patient.java:
@OneToMany(mappedBy = "patient") // "patient" refers to the field name in Visit.java
private List<Visit> visits;

// The OTHER SIDE — In Visit.java:
@ManyToOne
@JoinColumn(name = "patient_id")
private Patient patient;
```

---

## 7. Role-Based Access Control (RBAC)

### What is RBAC?

RBAC means **different users see different things** based on their role. It's like a hospital building:
- **Patients** can enter the waiting room and exam rooms
- **Doctors** can enter patient rooms, labs, and offices
- **Admins** can enter every room including the server room

### How RBAC Works in LabService

```
                         ┌─────────────────────────────┐
  User sends request ──► │  JwtAuthenticationFilter     │
  with JWT token         │                             │
                         │  1. Extract token from       │
                         │     "Authorization" header   │
                         │  2. Validate token signature │
                         │  3. Extract username + role  │
                         │  4. Set SecurityContext       │
                         └──────────────┬──────────────┘
                                        │
                                        ▼
                         ┌─────────────────────────────┐
                         │  SecurityConfig rules        │
                         │                             │
                         │  /api/patient/** → PATIENT?  │──► NO  → 403 Forbidden
                         │  /api/doctor/**  → DOCTOR?   │──► NO  → 403 Forbidden
                         │  /api/admin/**   → ADMIN?    │──► NO  → 403 Forbidden
                         │                             │
                         └──────────────┬──────────────┘
                                        │ YES
                                        ▼
                         ┌─────────────────────────────┐
                         │  Controller method           │
                         │                             │
                         │  @PreAuthorize checks:       │
                         │  "Is this THEIR data?"       │
                         │  Patient 1 can't see         │
                         │  Patient 2's records         │
                         └─────────────────────────────┘
```

### Two Levels of Protection

**Level 1: URL-based (SecurityConfig)** — Which portal can you access?
```java
.requestMatchers("/api/patient/**").hasRole("PATIENT")
.requestMatchers("/api/doctor/**").hasRole("DOCTOR")
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

**Level 2: Data-based (@PreAuthorize)** — Can you see THIS specific record?
```java
// In PatientVisitController — patient can only see THEIR OWN visits
@GetMapping("/visits")
@PreAuthorize("hasRole('PATIENT')")
public List<Visit> getMyVisits(@AuthenticationPrincipal UserDetails userDetails) {
    // userDetails.getUsername() → gets the logged-in user
    // Service layer filters: only return visits where patient.user.username == logged-in user
    return visitService.getVisitsForLoggedInPatient(userDetails.getUsername());
}

// In DoctorVisitController — doctor can only see visits THEY conducted
@GetMapping("/visits")
@PreAuthorize("hasRole('DOCTOR')")
public List<Visit> getMyVisits(@AuthenticationPrincipal UserDetails userDetails) {
    return visitService.getVisitsForLoggedInDoctor(userDetails.getUsername());
}

// In AdminVisitController — admin can see ALL visits
@GetMapping("/visits")
@PreAuthorize("hasRole('ADMIN')")
public List<Visit> getAllVisits() {
    return visitService.getAllVisits();
}
```

### Login Flow with Portals

```
1. User submits: POST /api/auth/login
   { "username": "john", "password": "secret123" }

2. AuthService:
   a. Find user by username in database
   b. Verify password with BCrypt
   c. Check if account is enabled
   d. Generate JWT token with role embedded
   e. Return token + role to client

3. Response:
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "username": "john",
     "role": "ROLE_PATIENT",
     "redirectUrl": "/patient/dashboard"
   }

4. Frontend reads the role and redirects:
   - ROLE_PATIENT → /patient/dashboard
   - ROLE_DOCTOR  → /doctor/dashboard
   - ROLE_ADMIN   → /admin/dashboard

5. Every subsequent API call includes the token:
   GET /api/patient/visits
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Registration Flow by Portal

```
POST /api/auth/register
{
  "username": "jane",
  "password": "secure456",
  "email": "jane@email.com",
  "portalType": "PATIENT",          ← User selects their portal
  "firstName": "Jane",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phone": "555-0123"
}

AuthService.register():
├── If portalType == "PATIENT":
│   ├── Create User with role = ROLE_PATIENT
│   ├── Create Patient profile linked to User
│   └── Return token (immediately active)
│
├── If portalType == "DOCTOR":
│   ├── Create User with role = ROLE_DOCTOR, enabled = false
│   ├── Create Doctor profile linked to User
│   └── Return message: "Account pending admin approval"
│   └── Admin must enable the account at /api/admin/users/{id}/enable
│
└── ROLE_ADMIN:
    └── Cannot self-register — only created by another admin
```

### Dashboard Responses (Different Data Per Portal)

**Patient Dashboard Response:**
```java
public class PatientDashboardResponse {
    private String patientName;
    private int upcomingAppointments;      // Count of future appointments
    private int activePrescriptions;       // Count of non-expired prescriptions
    private int pendingLabResults;         // Count of labs with status != COMPLETED
    private Appointment nextAppointment;   // The soonest upcoming appointment
    private List<Prescription> currentMedications;  // Active prescriptions
}
```

**Doctor Dashboard Response:**
```java
public class DoctorDashboardResponse {
    private String doctorName;
    private int todayAppointmentCount;     // Appointments scheduled for today
    private int totalPatients;             // Count of assigned patients
    private int pendingLabOrders;          // Labs they ordered that aren't done
    private List<Appointment> todaySchedule;  // Today's appointment list
    private List<LabOrder> recentLabResults;  // Recently completed lab results
}
```

**Admin Dashboard Response:**
```java
public class AdminDashboardResponse {
    private int totalUsers;                // All users in the system
    private int totalDoctors;
    private int totalPatients;
    private int totalVisitsThisMonth;
    private int pendingDoctorApprovals;    // Doctor accounts waiting to be enabled
    private int labOrdersInProgress;
    private Map<String, Integer> visitsBySpecialty;   // Visits grouped by doctor specialty
    private Map<String, Integer> appointmentsByStatus; // SCHEDULED, COMPLETED, CANCELLED counts
}
```

---

## 8. Component Deep Dive (With Code Examples)

### 6.1 Model (Entity) Layer

**What it does:** Defines the shape of your data. Each class becomes a database table.

**Key annotations explained:**

```java
package com.labservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity                              // REQUIRED: Marks this class as a database table
@Table(name = "patients")            // OPTIONAL: Custom table name (default = class name)
public class Patient {

    @Id                              // REQUIRED: Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ↑ Auto-increment: database assigns 1, 2, 3... automatically
    private Long id;

    @Column(nullable = false)        // This column cannot be NULL in the database
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)           // No two patients can have the same email
    private String email;

    private String phone;            // No annotation = simple column, nullable

    private LocalDate dateOfBirth;   // Java date type → DATE column in database

    @OneToOne                        // This patient is linked to exactly one User
    @JoinColumn(name = "user_id")    // Creates a foreign key column "user_id"
    private User user;

    @ManyToOne                       // Many patients can belong to one doctor
    @JoinColumn(name = "doctor_id")
    private Doctor primaryDoctor;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    // ↑ One patient has many visits
    // ↑ "mappedBy" = the Visit class has a field called "patient"
    // ↑ "cascade" = if you delete a patient, delete their visits too
    private List<Visit> visits;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    // --- CONSTRUCTOR ---
    public Patient() {}  // REQUIRED: JPA needs a no-arg constructor

    // --- GETTERS AND SETTERS ---
    // Java convention: every field needs a get and set method
    // These let other classes read and modify the fields

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    // ... (same pattern for all fields)
}
```

**All 8 entity classes you need to create:**

#### Role.java (Enum)
```java
public enum Role {
    ROLE_PATIENT,       // Can only view their own data
    ROLE_DOCTOR,        // Can manage their own patients
    ROLE_ADMIN          // Can manage everything
}
// WHY an enum instead of a String?
//   - Compile-time safety: typos like "PATEINT" won't compile
//   - IDE autocomplete: Role.ROLE_PATIENT instead of guessing strings
//   - Easy to iterate: Role.values() gives you all roles
```

#### User.java
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;        // Will be stored as a BCrypt hash, NEVER plain text

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)    // Stores "ROLE_PATIENT" as text, not a number
    @Column(nullable = false)
    private Role role;              // ROLE_PATIENT, ROLE_DOCTOR, or ROLE_ADMIN

    private boolean enabled;        // Admin can disable accounts without deleting them

    @OneToOne(mappedBy = "user")
    private Patient patient;

    @OneToOne(mappedBy = "user")
    private Doctor doctor;          // Linked if role is ROLE_DOCTOR
}
```

#### Doctor.java
```java
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String specialty;       // "Cardiology", "Dermatology", etc.
    private String phone;
    private String licenseNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "primaryDoctor")
    private List<Patient> patients;

    @OneToMany(mappedBy = "doctor")
    private List<Visit> visits;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;
}
```

#### Address.java
```java
@Entity
@Table(name = "addresses")
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;        // For map display
    private Double longitude;       // For map display
}
```

#### Visit.java
```java
@Entity
@Table(name = "visits")
public class Visit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate visitDate;

    private String reason;          // "Annual checkup", "Flu symptoms"
    private String diagnosis;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
    private List<Prescription> prescriptions;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
    private List<LabOrder> labOrders;
}
```

#### Prescription.java
```java
@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicationName;  // "Amoxicillin", "Ibuprofen"

    private String dosage;          // "500mg"
    private String frequency;       // "Twice daily"
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;    // "Take with food"

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor prescribedBy;
}
```

#### LabOrder.java
```java
@Entity
@Table(name = "lab_orders")
public class LabOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testName;        // "Complete Blood Count", "Lipid Panel"

    private String testCode;        // "CBC", "LP"

    @Column(nullable = false)
    private String status;          // "ORDERED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    private LocalDate orderedDate;
    private LocalDate completedDate;
    private String results;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor orderedBy;
}
```

#### Appointment.java
```java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String status;          // "SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED"

    private String reason;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}
```

---

### 6.2 Repository Layer

**What it does:** Provides methods to read/write data from the database. You write an interface, Spring writes the implementation.

**How Spring generates SQL from method names:**

```
findByLastName("Smith")
  → SELECT * FROM patients WHERE last_name = 'Smith'

findByDoctorIdAndStatus(5, "SCHEDULED")
  → SELECT * FROM appointments WHERE doctor_id = 5 AND status = 'SCHEDULED'

findByVisitDateBetween(start, end)
  → SELECT * FROM visits WHERE visit_date BETWEEN start AND end

countByStatus("COMPLETED")
  → SELECT COUNT(*) FROM lab_orders WHERE status = 'COMPLETED'

existsByDoctorIdAndDateTime(5, dateTime)
  → SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
    FROM appointments WHERE doctor_id = 5 AND date_time = dateTime
```

**All repository interfaces:**

```java
// --- UserRepository.java ---
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

// --- PatientRepository.java ---
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    List<Patient> findByPrimaryDoctorId(Long doctorId);
    List<Patient> findByLastNameContainingIgnoreCase(String name);
}

// --- DoctorRepository.java ---
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialty(String specialty);
    List<Doctor> findByLastNameContainingIgnoreCase(String name);
}

// --- VisitRepository.java ---
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByPatientId(Long patientId);
    List<Visit> findByDoctorId(Long doctorId);
    List<Visit> findByPatientIdOrderByVisitDateDesc(Long patientId);
    List<Visit> findByVisitDateBetween(LocalDate start, LocalDate end);
}

// --- PrescriptionRepository.java ---
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByVisitId(Long visitId);
    List<Prescription> findByVisitPatientId(Long patientId);
    // ↑ Spring traverses: Prescription → Visit → Patient → id
    List<Prescription> findByEndDateAfter(LocalDate date);  // Active prescriptions
}

// --- LabOrderRepository.java ---
@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    List<LabOrder> findByPatientId(Long patientId);
    List<LabOrder> findByStatus(String status);
    List<LabOrder> findByPatientIdAndStatus(Long patientId, String status);
    List<LabOrder> findByOrderedDateBetween(LocalDate start, LocalDate end);
}

// --- AppointmentRepository.java ---
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdAndDateTimeBetween(
        Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientIdAndStatus(Long patientId, String status);
    boolean existsByDoctorIdAndDateTime(Long doctorId, LocalDateTime dateTime);
}
```

**Methods you get for FREE from JpaRepository:**

```java
// These methods exist without you writing ANY code:
repository.findAll();              // Get all records
repository.findById(id);           // Get one by ID → returns Optional<T>
repository.save(entity);           // Create new OR update existing
repository.deleteById(id);         // Delete by ID
repository.count();                // Count all records
repository.existsById(id);         // Check if exists
```

---

### 6.3 Service Layer

**What it does:** Contains business logic — the rules that make your app work correctly.

**Full example — AppointmentService.java:**

```java
package com.labservice.service;

import com.labservice.model.*;
import com.labservice.repository.*;
import com.labservice.dto.request.CreateAppointmentRequest;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service    // Tells Spring: "this is a service — manage its lifecycle"
public class AppointmentService {

    @Autowired  // Spring automatically injects the repository instance
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // --- GET ALL APPOINTMENTS ---
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // --- GET ONE BY ID ---
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        // ↑ Optional.orElseThrow(): if no appointment found, throw a custom error
    }

    // --- GET BY PATIENT ---
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    // --- BOOK NEW APPOINTMENT ---
    public Appointment bookAppointment(CreateAppointmentRequest request) {

        // STEP 1: Verify patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // STEP 2: Verify doctor exists
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // STEP 3: Business rule — no past appointments
        if (request.getDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book appointments in the past");
        }

        // STEP 4: Business rule — no double-booking the doctor
        boolean conflict = appointmentRepository
            .existsByDoctorIdAndDateTime(doctor.getId(), request.getDateTime());
        if (conflict) {
            throw new BadRequestException("Doctor already has an appointment at this time");
        }

        // STEP 5: Create and save the appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDateTime(request.getDateTime());
        appointment.setReason(request.getReason());
        appointment.setStatus("SCHEDULED");

        return appointmentRepository.save(appointment);
    }

    // --- CANCEL APPOINTMENT ---
    public Appointment cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }

    // --- UPDATE APPOINTMENT ---
    public Appointment updateAppointment(Long id, CreateAppointmentRequest request) {
        Appointment appointment = getAppointmentById(id);
        appointment.setDateTime(request.getDateTime());
        appointment.setReason(request.getReason());
        return appointmentRepository.save(appointment);
    }
}
```

---

### 6.4 Controller Layer

**What it does:** Maps HTTP requests to service methods. Defines your API endpoints.

**Full example — AppointmentController.java:**

```java
package com.labservice.controller;

import com.labservice.model.Appointment;
import com.labservice.dto.request.CreateAppointmentRequest;
import com.labservice.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                          // This class handles HTTP requests and returns JSON
@RequestMapping("/api/appointments")     // Base path for all endpoints in this controller
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // GET /api/appointments
    // Returns: list of all appointments as JSON array
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    // GET /api/appointments/5
    // The {id} in the path becomes the @PathVariable parameter
    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    // GET /api/appointments/patient/3
    // Get all appointments for a specific patient
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getByPatient(@PathVariable Long patientId) {
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    // POST /api/appointments
    // @RequestBody: Spring converts incoming JSON to CreateAppointmentRequest object
    // @Valid: Runs validation annotations (@NotNull, @Future, etc.) on the request
    // ResponseEntity: Lets you control the HTTP status code (201 Created)
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        Appointment created = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/appointments/5
    // Update an existing appointment
    @PutMapping("/{id}")
    public Appointment updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.updateAppointment(id, request);
    }

    // DELETE /api/appointments/5
    // Cancel an appointment (soft delete — changes status to CANCELLED)
    @DeleteMapping("/{id}")
    public Appointment cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }
}
```

---

### 6.5 DTO (Data Transfer Object) Layer

**What it does:** Controls what data is sent to/from your API.

```java
// --- CreateAppointmentRequest.java ---
package com.labservice.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CreateAppointmentRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Date and time is required")
    @Future(message = "Appointment must be in the future")    // Validation!
    private LocalDateTime dateTime;

    private String reason;

    // Getters and Setters...
}
```

**Why DTOs instead of using entities directly?**

```
Without DTOs (BAD):
  POST body → User entity → password field is exposed in responses!
  JSON response includes every field, even internal ones

With DTOs (GOOD):
  POST body → RegisterRequest (only username, password, email)
  Response  → UserResponse (id, username, email — NO password)
```

---

### 6.6 Exception Handling

**What it does:** Returns clean JSON errors instead of ugly stack traces.

```java
// --- ResourceNotFoundException.java ---
package com.labservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)    // Returns 404 status code
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// --- BadRequestException.java ---
@ResponseStatus(HttpStatus.BAD_REQUEST)  // Returns 400 status code
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

// --- GlobalExceptionHandler.java ---
package com.labservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice   // Catches exceptions from ALL controllers
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", ex.getMessage(),
            "status", 404,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", ex.getMessage(),
            "status", 400,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
```

**Without exception handler:**
```json
{
  "timestamp": "2026-03-15T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "trace": "java.lang.RuntimeException: Doctor not found\n\tat com.labservice..."
  // ↑ Ugly! Exposes internal code to the user!
}
```

**With exception handler:**
```json
{
  "error": "Doctor not found with id: 99",
  "status": 404,
  "timestamp": "2026-03-15T10:00:00"
}
```

---

### 6.7 Configuration

**application.properties — App settings:**

```properties
# Server port (default is 8080)
server.port=8080

# PostgreSQL database (persistent — data survives restarts)
spring.datasource.url=jdbc:postgresql://localhost:5432/labservicedb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=meeraramesh
spring.datasource.password=

# JPA/Hibernate settings
spring.jpa.hibernate.ddl-auto=update
# ↑ update: Creates tables if missing, adds new columns, keeps existing data
# ↑ Other options: "create-drop" (reset every restart), "validate" (just check)

spring.jpa.show-sql=true
# ↑ Prints generated SQL to the console (helpful for learning)

# Defer data.sql until after Hibernate creates tables
spring.jpa.defer-datasource-initialization=true

# Run data.sql even with a real database (default only runs for H2)
spring.sql.init.mode=always

# Jackson (JSON serializer) settings
spring.jackson.serialization.write-dates-as-timestamps=false

# JWT settings (for portal authentication)
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.expiration=86400000
# ↑ 86400000ms = 24 hours — token expires after this
```

**data.sql — Seed default admin account on startup:**

```sql
-- Creates a default admin so you can manage the system
-- Password is BCrypt hash of "admin123" — change in production!
-- ON CONFLICT DO NOTHING prevents duplicate errors on restart
INSERT INTO users (username, password, email, role, enabled)
VALUES ('admin', '$2b$10$HE0WP8Rk...', 'admin@labservice.com', 'ROLE_ADMIN', true)
ON CONFLICT (username) DO NOTHING;
```

**SecurityConfig.java — Portal-based access control:**

```java
package com.labservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity    // Enables @PreAuthorize on individual methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ↑ STATELESS: no server-side sessions, JWT token in every request
            .authorizeHttpRequests(auth -> auth
                // PUBLIC — no login required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // PATIENT PORTAL — only patients can access
                .requestMatchers("/api/patient/**").hasRole("PATIENT")

                // DOCTOR PORTAL — only doctors can access
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")

                // ADMIN PORTAL — only admins can access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else requires authentication (any role)
                .anyRequest().authenticated()
            )
            // Add JWT filter BEFORE Spring's default auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(f -> f.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**How .hasRole() works:**
- `.hasRole("PATIENT")` checks if user's role is `ROLE_PATIENT`
- Spring auto-prepends "ROLE_" to the string you pass
- If a DOCTOR tries to access `/api/patient/visits`, they get a `403 Forbidden`

**JwtTokenProvider.java — Creates and validates login tokens:**

```java
package com.labservice.security;

import com.labservice.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")          // Read from application.properties
    private String jwtSecret;

    @Value("${jwt.expiration}")      // Token validity in milliseconds
    private long jwtExpiration;

    // Generate a token after successful login
    public String generateToken(String username, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
            .setSubject(username)                    // WHO this token is for
            .claim("role", role.name())              // WHAT role they have
            .setIssuedAt(now)                        // WHEN it was created
            .setExpiration(expiryDate)               // WHEN it expires
            .signWith(key, SignatureAlgorithm.HS256) // SIGNED so it can't be tampered
            .compact();
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key).build()
            .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // Check if token is valid and not expired
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**JwtAuthenticationFilter.java — Checks token on every request:**

```java
package com.labservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                    CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // STEP 1: Extract token from "Authorization: Bearer <token>" header
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // STEP 2: If token exists and is valid, set authentication
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
            // ↑ Now Spring Security knows WHO this user is and WHAT role they have
        }

        // STEP 3: Continue to the next filter / controller
        filterChain.doFilter(request, response);
    }
}
```

**CustomUserDetailsService.java — Loads user from database for Spring Security:**

```java
package com.labservice.security;

import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),           // Account enabled/disabled by admin
            true, true, true,           // Account not expired, credentials not expired, not locked
            Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
                // ↑ Converts Role.ROLE_PATIENT to a Spring Security authority
            )
        );
    }
}
```

---

## 9. Build Phases (Step-by-Step)

### Phase 1: Project Setup ✅ COMPLETED
**Goal:** Get a running Spring Boot app with project structure

**What was done:**
1. Generated Maven project with `mvn archetype:generate`
2. Converted `pom.xml` to Spring Boot (added parent, starters, JWT dependencies)
3. Created `LabServiceApplication.java` entry point with `@SpringBootApplication`
4. Created `application.properties` with H2 database, JWT, and JSON settings
5. Created all sub-packages: model, repository, service, controller (auth/patient/doctor/admin), dto, security, exception
6. Verified compilation: BUILD SUCCESS

**What was learned:** Maven, pom.xml structure, Spring Boot starters, project layout, packages

---

### Phase 1.5: Model Layer ✅ COMPLETED
**Goal:** Create all database entities with relationships

**Files created:**
- `model/Role.java` — Enum: ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN
- `model/User.java` — username, password, email, role, enabled; @OneToOne to Patient and Doctor
- `model/Address.java` — street, city, state, zipCode, latitude, longitude
- `model/Doctor.java` — name, email, specialization, phoneNumber, hospitalAffiliation, yearsOfExperience, consultationFee; @OneToOne Address, @OneToOne User, @ElementCollection for insurance/hours/days, @OneToMany Visit and Appointment
- `model/Patient.java` — firstName, lastName, dateOfBirth, email, phoneNumber; @OneToOne Address, @OneToOne User, @ManyToOne Doctor, @ElementCollection for conditions/allergies/medications/emergencyContacts/insurance, @OneToMany Visit and Appointment
- `model/Visit.java` — visitDate, reason, diagnosis, notes, visitType; @ManyToOne Patient and Doctor, @OneToMany Prescription and LabOrder
- `model/Prescription.java` — medicationName, dosage, frequency, startDate, endDate, instructions; @ManyToOne Visit, Doctor, and Pharmacy
- `model/LabOrder.java` — testName, testCode, status, orderedDate, completedDate, results, notes; @ManyToOne Visit, Patient, and Doctor
- `model/Appointment.java` — dateTime (LocalDateTime), status, reason, notes; @ManyToOne Patient and Doctor
- `model/Pharmacy.java` — name, phoneNumber, email, licenseNumber; @OneToOne Address, @ElementCollection hoursOfOperation and servicesOffered
- Patient.java updated with `@ManyToOne Pharmacy preferredPharmacy`
- Prescription.java updated with `@ManyToOne Pharmacy pharmacy`

**Repositories added:**
- `repository/PharmacyRepository.java` — findByNameContainingIgnoreCase, findByLicenseNumber

**Services added:**
- `service/PharmacyService.java` — CRUD, search by name

**What was learned:** @Entity, @Table, @Id, @GeneratedValue, @Column, @OneToOne, @ManyToOne, @OneToMany, @ElementCollection, @Enumerated, @JoinColumn, @CascadeType, getter/setter patterns, LocalDate vs LocalDateTime, field types must match getter/setter types

**Compiled:** 28 source files, BUILD SUCCESS

---

### Phase 2a: Repository Layer ✅ COMPLETED
**Goal:** Create data access interfaces for all entities

**What is a Repository?**
An interface (not a class) that defines how to read/write data. You write method names, Spring generates the SQL automatically. Extends `JpaRepository<EntityType, IdType>` to get free CRUD methods (findAll, findById, save, deleteById, count, existsById).

**How Spring generates SQL from method names:**
- `findByUsername("john")` → `SELECT * FROM users WHERE username = 'john'`
- `findByPatientIdAndStatus(1, "SCHEDULED")` → `SELECT * FROM appointments WHERE patient_id = 1 AND status = 'SCHEDULED'`
- `existsByDoctorIdAndDateTime(5, dt)` → `SELECT COUNT(*) > 0 FROM appointments WHERE doctor_id = 5 AND date_time = dt`

**Files created (7 total):**
- `repository/UserRepository.java` — findByUsername, findByEmail, existsByUsername, existsByEmail
- `repository/PatientRepository.java` — findByUserId, findByPrimaryDoctorId, findByLastNameContainingIgnoreCase
- `repository/DoctorRepository.java` — findByUserId, findBySpecialization, findByNameContainingIgnoreCase
- `repository/VisitRepository.java` — findByPatientId, findByDoctorId, findByPatientIdOrderByVisitDateDesc, findByVisitDateBetween
- `repository/PrescriptionRepository.java` — findByVisitId, findByVisitPatientId, findByEndDateAfterOrEndDateIsNull, findByPrescribedById
- `repository/LabOrderRepository.java` — findByPatientId, findByStatus, findByPatientIdAndStatus, findByOrderedById
- `repository/AppointmentRepository.java` — findByPatientId, findByDoctorId, existsByDoctorIdAndDateTime, findByPatientIdAndStatus, findByDoctorIdAndDateTimeBetween

**Key concepts learned:**
- `interface` not `class` — Spring provides the implementation
- `@Repository` annotation — marks it as a data access component
- `extends JpaRepository<Entity, Long>` — gives free CRUD methods
- Method naming convention: `findBy` + `FieldName` + optional `And`/`Or`/`Between`/`OrderBy`
- Return types: `Optional<T>` (single), `List<T>` (multiple), `boolean` (exists)
- Filename must match class name exactly (capital letter)
- Method names must match actual entity field names

**Compiled:** 18 source files, BUILD SUCCESS

---

### Phase 2a.5: Service Layer ✅ COMPLETED
**Goal:** Create business logic layer with validation rules for all entities

**What is a Service?**
A class annotated with `@Service` that contains business rules and logic. Services sit between Controllers and Repositories. Controllers call services, services call repositories. Rules like "don't double-book a doctor" or "can't change status of a completed lab order" live here.

**Key concepts:**
- `@Service` — marks class as a Spring-managed business logic component
- `@Autowired` — dependency injection: Spring automatically provides repository instances
- A service can use MULTIPLE repositories (e.g. AppointmentService uses AppointmentRepo + PatientRepo + DoctorRepo)
- `Optional.orElseThrow()` — handle "not found" cases cleanly
- `save()` does double duty: new object = INSERT, existing object = UPDATE

**Files created (7 total):**
- `service/UserService.java` — enable/disable accounts, change role, CRUD
- `service/PatientService.java` — profile management, assign doctor, search by name
- `service/DoctorService.java` — profile management, search by specialization/name
- `service/VisitService.java` — record visits, get history per patient/doctor, date range queries
- `service/PrescriptionService.java` — write prescriptions, find active, end date validation
- `service/LabOrderService.java` — order labs, status lifecycle (ORDERED→IN_PROGRESS→COMPLETED/CANCELLED), add results
- `service/AppointmentService.java` — book with conflict detection, cancel, no past bookings

**Business rules implemented:**
- Appointment: can't book in the past, can't double-book a doctor
- Prescription: end date must be after start date
- LabOrder: can't change status of completed/cancelled orders, auto-sets completion date
- All services: verify referenced entities exist before creating (patient, doctor, visit)

**Compiled:** 25 source files, BUILD SUCCESS

---

### Phase 2b: Controller Layer ✅ COMPLETED
**Goal:** Create REST API endpoints organized by portal (auth, patient, doctor, admin)

**What is a Controller?**
A class annotated with `@RestController` that handles HTTP requests. Controllers are the "waiter" — they receive requests, delegate to services, and return responses. They don't contain business logic.

```
HTTP Request → Controller (waiter) → Service (chef) → Repository (pantry) → Database
HTTP Response ← Controller ← Service ← Repository ← Database
```

**Key annotations:**
- `@RestController` — marks class as an HTTP request handler that returns JSON
- `@RequestMapping("/api/patient")` — base URL prefix for all endpoints in this class
- `@GetMapping("/appointments")` — handles GET requests
- `@PostMapping`, `@PutMapping`, `@DeleteMapping` — handle POST, PUT, DELETE
- `@RequestBody` — parse JSON request body into a Java object
- `@PathVariable` — extract value from URL path (e.g., `/appointments/{id}`)
- `ResponseEntity<T>` — control HTTP status code (200 OK, 201 Created, 204 No Content)

**HTTP Methods = CRUD:**
- GET = Read, POST = Create, PUT = Update, DELETE = Delete

**Portal structure (4 controller groups):**
```
controller/
├── auth/
│   └── AuthController.java            — POST /api/auth/register, /api/auth/login
├── patient/
│   └── PatientPortalController.java   — GET/POST /api/patient/** (own data only)
├── doctor/
│   └── DoctorPortalController.java    — GET/POST /api/doctor/** (own patients)
└── admin/
    └── AdminPortalController.java     — GET/POST/PUT/DELETE /api/admin/** (everything)
```

**Same service, different scope per portal:**
- Patient: `findByPatientId(myId)` — "Show MY appointments"
- Doctor: `findByDoctorId(myId)` — "Show MY patients' appointments"
- Admin: `findAll()` — "Show ALL appointments"

**How controllers are built — the recipe:**
1. Open the service files and look at each `public` method
2. Ask: "Which portal should access this?" (Patient? Doctor? Admin?)
3. For each YES: pick HTTP method (GET/POST/PUT/DELETE), pick URL path, map service params to `@PathVariable` or `@RequestBody`, return `ResponseEntity`

**Files created so far:**
- `controller/patient/PatientPortalController.java` ✅ — 11 endpoints (mostly GET, patients read data)
- `controller/doctor/DoctorPortalController.java` ✅ — 14 endpoints (GET + POST/PUT, doctors create data)
- `controller/admin/AdminPortalController.java` ✅ — 30 endpoints (full CRUD on all entities)
- `controller/auth/AuthController.java` ✅ — 6 endpoints (register, login, google, social, forgot-password, reset-password)

**Key lessons learned:**
- Service methods take individual parameters (e.g. `bookAppointment(Long, Long, LocalDateTime, String)`) not entity objects. The controller must extract fields from `@RequestBody` to match the service signature.
- `@RequestParam` reads from query string (`/search?name=Smith`), `@PathVariable` reads from URL path (`/patients/5`)
- Admin controller uses 8 `@Autowired` services — one for each entity type it manages
- Same service, different scope per portal: Patient calls `getByPatientId()`, Doctor calls `getByDoctorId()`, Admin calls `getAll()`

**Data-level security applied:** Patient/Doctor controllers updated to read user ID from JWT token instead of URL path. Prevents Patient A from viewing Patient B's data by manipulating the URL.

**Compiled:** 40 source files, BUILD SUCCESS

---

### Phase 2c: Security Layer + Auth ✅ COMPLETED
**Goal:** Users can register as Patient or Doctor, log in, receive a JWT token, and access only their portal's endpoints

**How JWT authentication works:**
1. User logs in with username + password → server returns a signed JWT token
2. Token contains: `{ username, role, expiration }` signed with a secret key
3. User sends `Authorization: Bearer <token>` header on every future request
4. JwtAuthenticationFilter reads token → validates → tells Spring Security who the user is
5. SecurityConfig checks role against URL rules:
   - `/api/auth/**` → public (no login)
   - `/api/patient/**` → ROLE_PATIENT only
   - `/api/doctor/**` → ROLE_DOCTOR only
   - `/api/admin/**` → ROLE_ADMIN only

**Files created (9 new + 1 seed data):**

Security layer (4 files):
- `security/SecurityConfig.java` ✅ — URL-to-role rules, BCrypt encoder, stateless sessions, CSRF disabled
- `security/JwtTokenProvider.java` ✅ — generateToken(), getUsernameFromToken(), getRoleFromToken(), validateToken()
- `security/JwtAuthenticationFilter.java` ✅ — OncePerRequestFilter that reads Bearer token on every request
- `security/CustomUserDetailsService.java` ✅ — Bridge: loads YOUR User entity → converts to Spring Security's UserDetails

Auth controller + service (2 files):
- `service/AuthService.java` ✅ — register() hashes password + assigns role + auto-creates Patient profile; login() checks credentials + generates token
- `controller/auth/AuthController.java` ✅ — POST /api/auth/register (201 Created), POST /api/auth/login (200 + token)

DTOs (3 files):
- `dto/request/RegisterRequest.java` ✅ — { username, password, email, portalType }
- `dto/request/LoginRequest.java` ✅ — { username, password }
- `dto/response/LoginResponse.java` ✅ — { token, username, role }

Seed data:
- `resources/data.sql` ✅ — default admin account (admin/admin123)

**Key concepts learned:**
- **BCrypt** — one-way password hashing, can't reverse hash back to password
- **JWT** — signed token with username + role + expiration
- **@Component** — like @Service but for generic utilities (JwtTokenProvider)
- **@Value("${key}")** — reads values from application.properties
- **@Bean** — method returns a Spring-managed object other classes can @Autowired
- **@Configuration + @EnableWebSecurity** — marks the security config class
- **SecurityFilterChain** — defines URL access rules
- **OncePerRequestFilter** — base class for per-request filters
- **SecurityContextHolder** — global holder for the current authenticated user
- **DTOs** — protect entities from unwanted data, define clean JSON shapes

**Compiled:** 40 source files, BUILD SUCCESS

---

### Phase 2d: Data-Level Security ✅ COMPLETED
**Goal:** Ensure patients can only see THEIR OWN data and doctors can only see THEIR OWN patients/records

**The problem:** URL-level security (Phase 2c) only checks "are you a patient?" not "is this YOUR data?" A patient could change the ID in the URL (`/visits/5` → `/visits/9`) and see another patient's visits.

**The fix:** Removed user IDs from URL paths. Controllers now read the logged-in user's identity from the JWT token via `SecurityContextHolder`, then look up their Patient/Doctor profile.

```
OLD: GET /api/patient/visits/{patientId}  → patient controls the ID (insecure)
NEW: GET /api/patient/visits              → server reads ID from token (secure)
```

**Helper method pattern (added to both Patient and Doctor controllers):**
```java
private Patient getLoggedInPatient() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userService.getUserByUsername(username);
    return patientService.getPatientByUserId(user.getId());
}
// Chain: JWT token → username → User → Patient
```

**Key concept — SecurityContextHolder:**
A global holder set by JwtAuthenticationFilter on every request. By the time your controller runs, it already contains the authenticated user's identity. Calling `.getName()` returns the username from the validated JWT token — can't be faked because the token signature is verified first.

**What changed:**
- PatientPortalController: all 11 endpoints now use `getLoggedInPatient()` instead of `@PathVariable Long patientId`
- DoctorPortalController: all 14 endpoints now use `getLoggedInDoctor()` instead of `@PathVariable Long doctorId`
- POST endpoints (book appointment, record visit, write Rx, order lab): force the patient/doctor ID from the token so users can't impersonate others
- DELETE appointment: verifies the appointment belongs to the logged-in patient before cancelling
- AdminPortalController: NOT changed — admins are supposed to see everyone's data

**The rule:**
- Patient/Doctor controllers: ID comes from TOKEN (can only see own data)
- Admin controller: ID comes from URL (can see everyone's data)

---

### Phase 2e: Google Login (OAuth2) ✅ COMPLETED
**Goal:** Users can log in with their Google account instead of username + password

**How it works:**
1. Frontend shows "Sign in with Google" button
2. User clicks → Google popup → authenticates → Google gives a token
3. Frontend sends token to `POST /api/auth/google`
4. Server verifies token with Google → extracts email + name
5. Finds or creates user → generates our JWT token → returns it
6. From here on, identical to normal login (same JWT, same security)

**Files created/modified:**
- `dto/request/GoogleLoginRequest.java` ✅ — { googleToken, portalType }
- `service/AuthService.java` ✅ — added `googleLogin()` method (verify Google token, find-or-create user)
- `controller/auth/AuthController.java` ✅ — added `POST /api/auth/google` endpoint
- `pom.xml` ✅ — added `google-api-client` dependency
- `application.properties` ✅ — added `google.client-id` setting

**Key concepts:**
- **OAuth2** — "let someone else handle the login" (Google verifies the password, not us)
- **GoogleIdTokenVerifier** — Google's official library to verify tokens (checks signature, Client ID, expiry)
- **Google Client ID** — identifier from Google Cloud Console that says "this is my app"
- **Find-or-create pattern** — check if email exists → if yes, log in; if no, create account
- **Random password for Google users** — `UUID.randomUUID()` hashed with BCrypt (user never uses it)

**Six auth endpoints now:**
```
POST /api/auth/register         → Create account (username + password)
POST /api/auth/login            → Log in (username + password → JWT)
POST /api/auth/google           → Log in (Google token → JWT)
POST /api/auth/social           → Log in with any provider (Google/Facebook/GitHub → JWT)
POST /api/auth/forgot-password  → Request password reset (email → reset token)
POST /api/auth/reset-password   → Reset password (token + newPassword → updated)
```

**Compiled:** 41 source files, BUILD SUCCESS

---

### Phase 2f: Social Logins (Facebook + GitHub) ✅ COMPLETED
**Goal:** Users can log in with Facebook or GitHub in addition to Google

**How it works:** Same pattern as Google — frontend gets token from provider, sends to backend, backend verifies with provider, finds/creates user, returns our JWT.

**Files created:**
- `dto/request/SocialLoginRequest.java` ✅ — generic DTO: { provider, accessToken, portalType }
- `service/SocialUserInfo.java` ✅ — normalized user info from any provider: { email, firstName, lastName }
- `service/SocialAuthService.java` ✅ — verifies tokens for Google (library), Facebook (API call), GitHub (API call)

**Files modified:**
- `service/AuthService.java` ✅ — added `socialLogin()` that delegates to SocialAuthService
- `controller/auth/AuthController.java` ✅ — added `POST /api/auth/social` unified endpoint

**Key concepts:**
- **RestTemplate** — Spring's HTTP client for calling external APIs (Facebook Graph API, GitHub API)
- **SocialUserInfo** — normalizes data from all providers into one shape
- **switch expression** — routes to the right provider: `case "facebook" -> verifyFacebook()`
- **Unified endpoint** — one `/social` endpoint handles all providers; adding Apple/Twitter = just add verification logic

**How each provider verifies:**
```
Google   → GoogleIdTokenVerifier library (verifies signed JWT)
Facebook → GET graph.facebook.com/me?access_token=X (token in URL)
GitHub   → GET api.github.com/user with Bearer header (token in header)
           + GET api.github.com/user/emails if email is private
```

**Compiled:** 44 source files, BUILD SUCCESS

---

### Phase 2g: Forgot Password & Reset Password ✅ COMPLETED
**Goal:** Users who forgot their password can request a reset link and set a new password

**How the full flow works:**
```
Step 1 — Forgot Password:
  User enters email → POST /api/auth/forgot-password
  Server generates random UUID token → saves to user record with 30-min expiry
  In production: sends email with link https://yourapp.com/reset?token=abc123
  Always returns same message (prevents user enumeration attack)

Step 2 — Reset Password:
  User clicks email link → enters new password → POST /api/auth/reset-password
  Server finds user by token → checks expiry → hashes new password → saves
  Clears token (one-time use) → returns success
```

**Files created:**
- `dto/request/ForgotPasswordRequest.java` ✅ — { email }
- `dto/request/ResetPasswordRequest.java` ✅ — { token, newPassword }

**Files modified:**
- `model/User.java` ✅ — added `resetToken` (String) + `resetTokenExpiry` (LocalDateTime) fields
- `repository/UserRepository.java` ✅ — added `findByResetToken(String resetToken)` query method
- `service/AuthService.java` ✅ — added `forgotPassword()` + `resetPassword()` methods
- `controller/auth/AuthController.java` ✅ — added `POST /forgot-password` + `POST /reset-password` endpoints

**Key concepts:**
- **UUID** — `UUID.randomUUID().toString()` generates a unique random string (e.g., "550e8400-e29b-41d4-a716-446655440000")
- **LocalDateTime.now().plusMinutes(30)** — sets token expiry 30 minutes in the future
- **isBefore()** — checks if a time has passed (is the expiry in the past?)
- **User enumeration prevention** — always return same message regardless of whether email exists
- **One-time use tokens** — clear token after use so the link can't be reused

**Why there's no logout endpoint:**
JWT is stateless — the server doesn't track sessions. The token IS the session.
Logout = the frontend deletes the token from localStorage. No server call needed.
If a token is stolen, it works until it expires (mitigated by short expiry times).

**Compiled:** BUILD SUCCESS

---

### Phase 2h: Exception Handling Layer ✅ COMPLETED
**Goal:** Replace all `RuntimeException` with proper custom exceptions and return clean JSON error responses with correct HTTP status codes

**The problem before:**
- Every error was `throw new RuntimeException("...")` — all returned HTTP 500
- Frontend got ugly stack traces or inconsistent error formats
- No way to tell the difference between "not found" and "bad input"

**The solution:**
```
BEFORE (everything was 500):                    AFTER (correct status codes):
──────────────────────────                      ─────────────────────────────
RuntimeException("Patient not found")  → 500   ResourceNotFoundException  → 404 Not Found
RuntimeException("Email already taken") → 500  DuplicateResourceException → 409 Conflict
RuntimeException("Cannot book in past") → 500  BadRequestException        → 400 Bad Request
RuntimeException("Not your appointment")→ 500  UnauthorizedException      → 401 Unauthorized
Any unexpected crash                    → 500   Exception (catch-all)      → 500 Internal Error
```

**Files created:**
```
exception/
├── ResourceNotFoundException.java   ✅ → 404 (thing doesn't exist in database)
├── BadRequestException.java         ✅ → 400 (invalid data or business rule violation)
├── DuplicateResourceException.java  ✅ → 409 (trying to create something that exists)
├── UnauthorizedException.java       ✅ → 401 (accessing something you don't own)
├── ErrorResponse.java               ✅ → Clean JSON shape: { status, error, message, timestamp }
└── GlobalExceptionHandler.java      ✅ → @ControllerAdvice that catches all exceptions
```

**Files modified (replaced RuntimeException):**
- `service/UserService.java` ✅ — ResourceNotFoundException
- `service/PatientService.java` ✅ — ResourceNotFoundException
- `service/DoctorService.java` ✅ — ResourceNotFoundException
- `service/PharmacyService.java` ✅ — ResourceNotFoundException
- `service/VisitService.java` ✅ — ResourceNotFoundException
- `service/PrescriptionService.java` ✅ — ResourceNotFoundException + BadRequestException
- `service/LabOrderService.java` ✅ — ResourceNotFoundException + BadRequestException
- `service/AppointmentService.java` ✅ — ResourceNotFoundException + BadRequestException
- `service/AuthService.java` ✅ — DuplicateResourceException + UnauthorizedException + ResourceNotFoundException + BadRequestException
- `service/SocialAuthService.java` ✅ — UnauthorizedException + BadRequestException
- `controller/patient/PatientPortalController.java` ✅ — UnauthorizedException

**Key concepts:**
- **@ControllerAdvice** — "apply this to ALL controllers" — one class catches exceptions from everywhere
- **@ExceptionHandler** — "when THIS exception is thrown, run THIS method"
- **ErrorResponse** — consistent JSON shape so the frontend always knows what to expect
- **Catch-all handler** — `@ExceptionHandler(Exception.class)` catches anything unexpected, hides internal details from users

**Error response example:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id: 5",
  "timestamp": "2026-03-15T14:30:00"
}
```

**Compiled:** BUILD SUCCESS

---

### Security Layer — File Summary

```
security/
├── JwtTokenProvider.java          — Token Factory: create, read, validate JWT tokens
│                                    @Component, @Value for secret/expiration
│
├── CustomUserDetailsService.java  — Translator: YOUR User → Spring Security's UserDetails
│                                    implements UserDetailsService interface
│
├── JwtAuthenticationFilter.java   — Bouncer: runs BEFORE every request
│                                    extends OncePerRequestFilter
│                                    reads token → validates → sets SecurityContextHolder
│
└── SecurityConfig.java            — Rule Book: URL→role mapping, BCrypt, stateless sessions
                                     @Configuration, @EnableWebSecurity, @Bean methods
```

### Service Layer — File Summary

```
service/
├── UserService.java          — Account CRUD, enable/disable, change role (Admin)
├── PatientService.java       — Patient profiles, assign doctor, search (All portals)
├── DoctorService.java        — Doctor profiles, specialization search (All portals)
├── VisitService.java         — Record visits, view history (Doctor creates, Patient reads)
├── PrescriptionService.java  — Write Rx, find active, date validation (Doctor creates, Patient reads)
├── LabOrderService.java      — Order labs, status lifecycle, add results (Doctor creates, Patient reads)
├── AppointmentService.java   — Book/cancel with conflict detection (Patient books, Doctor views)
├── PharmacyService.java      — Pharmacy CRUD and search (Admin)
├── AuthService.java          — Register, login, googleLogin, socialLogin, forgotPassword, resetPassword (Auth)
├── SocialAuthService.java    — Verify tokens for Google/Facebook/GitHub (Auth)
├── SocialUserInfo.java       — Normalized social user data container
├── EmailService.java         — Send emails (password reset links, notifications)
└── DashboardService.java     — Assemble dashboard stats for each portal
```

---

### Phase 2i: Email Service ✅ COMPLETED
**Goal:** Send real emails for password reset (replaces System.out.println)

**Files created:**
- `service/EmailService.java` ✅ — `sendPasswordResetEmail()` + generic `sendEmail()` utility

**Files modified:**
- `pom.xml` ✅ — added `spring-boot-starter-mail` dependency
- `application.properties` ✅ — added SMTP settings (host, port, username, password)
- `service/AuthService.java` ✅ — replaced `System.out.println()` with `emailService.sendPasswordResetEmail()`

**Key concepts:**
- **JavaMailSender** — Spring's email-sending tool, auto-configured from properties
- **SimpleMailMessage** — plain text email (to, from, subject, body)
- **App passwords** — Gmail requires a special app password (not your regular password)
- **Separation of concerns** — AuthService doesn't know HOW to send emails, it delegates to EmailService

---

### Phase 2j: Dashboard Endpoints ✅ COMPLETED
**Goal:** Each portal has a dashboard showing personalized stats

**Files created:**
- `dto/response/PatientDashboardResponse.java` ✅ — upcoming appointments, active Rx, pending labs, next appointment
- `dto/response/DoctorDashboardResponse.java` ✅ — total patients, today's schedule, pending labs
- `dto/response/AdminDashboardResponse.java` ✅ — system-wide counts (users, patients, doctors, visits, labs, Rx, pharmacies)
- `service/DashboardService.java` ✅ — assembles dashboard data from multiple repositories

**Files modified:**
- `controller/patient/PatientPortalController.java` ✅ — added `GET /api/patient/dashboard`
- `controller/doctor/DoctorPortalController.java` ✅ — added `GET /api/doctor/dashboard`
- `controller/admin/AdminPortalController.java` ✅ — added `GET /api/admin/dashboard`

**Key concepts:**
- **Stream API** — `.stream().filter().min().collect()` for filtering and sorting lists in Java
- **Dashboard DTOs** — shape the response for the frontend without exposing raw entities
- **Aggregation** — combining data from multiple repositories into one response

---

### Phase 2k: Swagger / OpenAPI Documentation ✅ COMPLETED
**Goal:** Auto-generated, interactive API documentation at /swagger-ui.html

**Files created:**
- `config/SwaggerConfig.java` ✅ — OpenAPI config with app info + JWT auth button

**Files modified:**
- `pom.xml` ✅ — added `springdoc-openapi-starter-webmvc-ui` dependency
- `application.properties` ✅ — Swagger UI and API docs paths
- `security/SecurityConfig.java` ✅ — permitAll() for /swagger-ui/**, /v3/api-docs/**

**How to use:**
```
1. Start the app: mvn spring-boot:run
2. Open browser: http://localhost:8080/swagger-ui.html
3. See all endpoints listed with HTTP methods, parameters, body shapes
4. Click "Authorize" → paste JWT token → test protected endpoints
5. OpenAPI JSON spec: http://localhost:8080/v3/api-docs
```

---

### Phase 2l: JUnit Tests ✅ COMPLETED
**Goal:** Unit tests for service layer business logic

**Files created:**
```
src/test/java/com/labservice/service/
├── UserServiceTest.java          ✅ — 9 tests (CRUD, enable/disable, role change, not-found)
├── AppointmentServiceTest.java   ✅ — 7 tests (booking rules, past date, double-book, cancel)
├── AuthServiceTest.java          ✅ — 9 tests (register, duplicates, forgot/reset password, token expiry)
└── LabOrderServiceTest.java      ✅ — 6 tests (status lifecycle, can't change completed, add results)
```

**Test results:** 31 tests, 0 failures, BUILD SUCCESS

**Key concepts:**
- **@ExtendWith(MockitoExtension.class)** — enables Mockito mocking framework
- **@Mock** — creates a fake version of a dependency (doesn't hit database)
- **@InjectMocks** — creates real service with fakes injected
- **when().thenReturn()** — tell the mock what to return when called
- **assertThrows()** — verify that an exception is thrown
- **verify()** — check that a mock method was actually called

---

### Phase 3: React/TypeScript Frontend ✅ COMPLETED
**Goal:** Build a full React frontend with TypeScript that connects to the Spring Boot backend

**Tech stack:**
- **React 19** — UI library for building component-based interfaces
- **TypeScript** — adds type safety to JavaScript (catches errors at compile time)
- **Vite** — fast build tool and dev server (replaces old Webpack/Create React App)
- **React Router** — client-side routing (navigates between pages without full page reload)
- **Axios** — HTTP client for calling the Spring Boot API
- **Google Identity Services** — Google's official "Sign in with Google" library for OAuth

**Project structure:**
```
frontend/
├── index.html                  — Loads Google Identity Services script for "Sign in with Google"
├── .env                        — VITE_GOOGLE_CLIENT_ID (your Google OAuth Client ID)
├── vite.config.ts              — Dev server config (port 3000, proxy /api → :8080)
├── tsconfig.app.json           — TypeScript compiler settings
├── src/
│   ├── main.tsx                — Entry point: wraps App in BrowserRouter + AuthProvider
│   ├── App.tsx                 — Route definitions for all pages
│   ├── index.css               — Global styles (navbar, cards, tables, forms, social login, status badges)
│   ├── api/
│   │   ├── client.ts           — Axios instance with JWT interceptor (auto-attaches Bearer token)
│   │   ├── auth.ts             — Login, register, googleLogin, socialLogin, forgot/reset password + interfaces
│   │   ├── patient.ts          — Patient portal API calls (dashboard, profile, appointments, visits, Rx, labs)
│   │   ├── doctor.ts           — Doctor portal API calls (dashboard, patients, visits, Rx, labs, appointments)
│   │   └── admin.ts            — Admin portal API calls (dashboard, users, patients, doctors, appointments)
│   ├── context/
│   │   └── AuthContext.tsx      — React Context for auth state (login, googleLogin, socialLogin, logout)
│   ├── components/
│   │   ├── Navbar.tsx           — Top nav with role-based links (different menu per role)
│   │   └── ProtectedRoute.tsx   — Route guard: redirects to /login if not authenticated, / if wrong role
│   └── pages/
│       ├── auth/
│       │   ├── LoginPage.tsx          — Login form + "Sign in with Google" button → role-based redirect
│       │   ├── RegisterPage.tsx       — Registration form + Google signup (portal type applies to both)
│       │   ├── ForgotPasswordPage.tsx — Email input → sends reset link
│       │   └── ResetPasswordPage.tsx  — New password form (reads token from URL query param)
│       ├── patient/
│       │   ├── PatientDashboard.tsx    — Stats cards + upcoming appointments + medications + labs
│       │   ├── PatientAppointments.tsx — View/book/cancel appointments
│       │   ├── PatientVisits.tsx       — View visit history
│       │   ├── PatientPrescriptions.tsx— View prescriptions + active-only filter
│       │   ├── PatientLabOrders.tsx    — View lab orders + status filter (ALL/ORDERED/IN_PROGRESS/COMPLETED)
│       │   ├── PatientProfile.tsx      — View/edit profile (phone, address, DOB, gender, blood group)
│       │   └── PatientDoctor.tsx       — View assigned primary doctor info
│       ├── doctor/
│       │   ├── DoctorDashboard.tsx     — Stats cards + today's appointments + pending labs
│       │   ├── DoctorPatients.tsx      — View assigned patients
│       │   ├── DoctorAppointments.tsx  — View appointment schedule
│       │   ├── DoctorVisits.tsx        — View/record/edit visits (inline editing)
│       │   ├── DoctorPrescriptions.tsx — View/write prescriptions
│       │   ├── DoctorLabOrders.tsx     — View/order labs, update status, add results
│       │   └── DoctorProfile.tsx       — View/edit doctor profile (specialization, phone, hospital, fee)
│       └── admin/
│           ├── AdminDashboard.tsx      — System-wide stats (10 metric cards)
│           ├── AdminUsers.tsx          — Full CRUD: enable/disable/delete + change role
│           ├── AdminPatients.tsx       — Full CRUD: create/edit/delete/search + assign doctor
│           ├── AdminDoctors.tsx        — Full CRUD: create/edit/delete/search
│           ├── AdminAppointments.tsx   — View + cancel + delete appointments
│           ├── AdminVisits.tsx         — View + date range filter + delete visits
│           ├── AdminPrescriptions.tsx  — View + delete prescriptions
│           ├── AdminLabOrders.tsx      — View + status filter + delete lab orders
│           └── AdminPharmacies.tsx     — Full CRUD: create/edit/delete/search pharmacies
```

**Key concepts:**
- **Vite Proxy** — `proxy: { '/api': 'http://localhost:8080' }` forwards API calls to Spring Boot (avoids CORS issues in development)
- **Axios Interceptors** — request interceptor auto-attaches `Authorization: Bearer <token>` header; response interceptor handles 401 (expired token → redirect to login)
- **React Context** — global state management without prop drilling. AuthProvider wraps the entire app so any component can call `useAuth()` to get user, login, logout
- **Protected Routes** — `<ProtectedRoute requiredRole="ROLE_PATIENT">` checks auth + role before rendering the page
- **TypeScript Interfaces** — `LoginRequest`, `LoginResponse`, `RegisterRequest` etc. define the exact shape of API data
- **`verbatimModuleSyntax`** — TypeScript setting that requires `import type` for type-only imports (prevents bundler issues)
- **Google Identity Services** — Google's "Sign in with Google" library loaded via `<script>` in index.html. Renders the official Google button, handles the popup, and returns a signed credential (ID token) that our backend verifies
- **`import.meta.env.VITE_GOOGLE_CLIENT_ID`** — Vite exposes env vars prefixed with `VITE_` to the frontend. Set your Google Client ID in `.env`
- **Social login role assignment** — On first Google/social login, the backend auto-creates an account. The `portalType` field tells it whether to make a patient or doctor. Subsequent logins reuse the existing account and role

**How the frontend and backend connect:**
```
─── REGULAR LOGIN ───
Browser (port 3000)                    Spring Boot (port 8080)
─────────────────────                  ────────────────────────
User fills login form
  → POST /api/auth/login  ──proxy──→   AuthController.login()
  ← { token, username, role }          AuthService verifies password
  → saves token to localStorage        JwtTokenProvider generates JWT

─── GOOGLE SIGN-IN ───
Browser                    Google                     Spring Boot
───────                    ──────                     ───────────
User clicks "Sign in
with Google" button
  → Google popup opens → user logs into Google
  ← Google returns credential (ID token)
  → POST /api/auth/google ──────────────────────→   AuthController.googleLogin()
     { googleToken, portalType }                     GoogleIdTokenVerifier.verify()
  ← { token, username, role }                        finds/creates user
  → saves OUR JWT to localStorage                    JwtTokenProvider generates JWT

─── AFTER LOGIN (same for both) ───
User clicks "Appointments"
  → GET /api/patient/appointments ──→  PatientPortalController.getAppointments()
     (Axios adds Bearer header)        (JwtAuthFilter validates token)
  ← [ { id, date, reason, ... } ]     (SecurityConfig checks ROLE_PATIENT)
  → renders table with data

─── ROLE-BASED ACCESS CONTROL ───
Frontend (ProtectedRoute)              Backend (SecurityConfig)
─────────────────────────              ────────────────────────
ROLE_PATIENT → /patient/*             /api/patient/** → hasRole('PATIENT')
ROLE_DOCTOR  → /doctor/*              /api/doctor/**  → hasRole('DOCTOR')
ROLE_ADMIN   → /admin/*               /api/admin/**   → hasRole('ADMIN')
```

**Build verified:** `npx vite build` → 109 modules transformed, 0 errors

---

### Original Phases 3–12 — All Completed

The original plan had Phases 3–12 as separate steps (Doctor, Patient, Visits, Prescriptions, Lab Orders, Appointments, Dashboards, Admin, Exceptions, Testing). In practice, the Model + Repository + Service layers were built together in Phase 1.5–2a.5, then Controllers were consolidated into portal-based files (one per portal) in Phase 2b, and Security/Auth/Social/Password Reset/Exceptions followed in Phases 2c–2h.

**Everything from the original Phases 3–12 is now ✅ Complete.**

---

## 10. API Endpoints (Full List — Organized by Portal)

### Public Endpoints (No Login Required)
| Method | URL                           | Description              |
|--------|-------------------------------|--------------------------|
| POST   | /api/auth/register            | Create new account (select patient/doctor) |
| POST   | /api/auth/login               | Log in, receive JWT token + role |
| POST   | /api/auth/google              | Log in with Google account |
| POST   | /api/auth/social              | Log in with any social provider (Google/Facebook/GitHub) |
| POST   | /api/auth/forgot-password     | Request password reset (sends token) |
| POST   | /api/auth/reset-password      | Reset password with token |

### Patient Portal (ROLE_PATIENT)
| Method | URL                              | Description                        |
|--------|----------------------------------|------------------------------------|
| GET    | /api/patient/profile             | View own profile                   |
| PUT    | /api/patient/profile             | Update own profile                 |
| GET    | /api/patient/visits              | View own visit history             |
| GET    | /api/patient/prescriptions       | View all own prescriptions         |
| GET    | /api/patient/prescriptions/active| View current active prescriptions  |
| GET    | /api/patient/lab-orders          | View own lab orders + results      |
| GET    | /api/patient/lab-orders/status/{status} | View lab orders by status   |
| GET    | /api/patient/appointments        | View own appointments              |
| POST   | /api/patient/appointments        | Book a new appointment             |
| DELETE | /api/patient/appointments/{id}   | Cancel an appointment (own only)   |
| GET    | /api/patient/doctors             | View assigned doctor info          |

### Doctor Portal (ROLE_DOCTOR)
| Method | URL                              | Description                        |
|--------|----------------------------------|------------------------------------|
| GET    | /api/doctor/profile              | View own profile                   |
| PUT    | /api/doctor/profile              | Update own profile                 |
| GET    | /api/doctor/patients             | View own assigned patients         |
| GET    | /api/doctor/visits               | View visits conducted by self      |
| POST   | /api/doctor/visits               | Record a new visit                 |
| PUT    | /api/doctor/visits/{id}          | Update visit notes/diagnosis       |
| GET    | /api/doctor/prescriptions        | View prescriptions written by self |
| POST   | /api/doctor/prescriptions        | Write a new prescription           |
| GET    | /api/doctor/lab-orders           | View lab orders created by self    |
| POST   | /api/doctor/lab-orders           | Order a new lab test               |
| PUT    | /api/doctor/lab-orders/{id}/status  | Update lab order status         |
| PUT    | /api/doctor/lab-orders/{id}/results | Add results to lab order        |
| GET    | /api/doctor/appointments         | View own appointment schedule      |

### Admin Portal (ROLE_ADMIN)
| Method | URL                              | Description                        |
|--------|----------------------------------|------------------------------------|
| GET    | /api/admin/users                 | List all user accounts             |
| GET    | /api/admin/users/{id}            | View specific user                 |
| PUT    | /api/admin/users/{id}/enable     | Enable user account                |
| PUT    | /api/admin/users/{id}/disable    | Disable user account               |
| PUT    | /api/admin/users/{id}/role       | Change user role                   |
| DELETE | /api/admin/users/{id}            | Delete user account                |
| GET    | /api/admin/patients              | List all patients                  |
| GET    | /api/admin/patients/{id}         | View specific patient              |
| POST   | /api/admin/patients              | Create patient profile             |
| PUT    | /api/admin/patients/{id}         | Update any patient                 |
| PUT    | /api/admin/patients/{id}/assign-doctor | Assign doctor to patient    |
| GET    | /api/admin/patients/search       | Search patients by name            |
| DELETE | /api/admin/patients/{id}         | Delete patient                     |
| GET    | /api/admin/doctors               | List all doctors                   |
| GET    | /api/admin/doctors/{id}          | View specific doctor               |
| POST   | /api/admin/doctors               | Create doctor profile              |
| PUT    | /api/admin/doctors/{id}          | Update any doctor                  |
| GET    | /api/admin/doctors/search        | Search doctors by name             |
| DELETE | /api/admin/doctors/{id}          | Delete doctor                      |
| GET    | /api/admin/visits                | View all visits                    |
| POST   | /api/admin/visits                | Create a visit                     |
| PUT    | /api/admin/visits/{id}           | Update a visit                     |
| DELETE | /api/admin/visits/{id}           | Delete a visit                     |
| GET    | /api/admin/prescriptions         | View all prescriptions             |
| POST   | /api/admin/prescriptions         | Create a prescription              |
| DELETE | /api/admin/prescriptions/{id}    | Delete a prescription              |
| GET    | /api/admin/lab-orders            | View all lab orders                |
| POST   | /api/admin/lab-orders            | Create a lab order                 |
| PUT    | /api/admin/lab-orders/{id}/status| Update lab order status            |
| DELETE | /api/admin/lab-orders/{id}       | Delete a lab order                 |
| GET    | /api/admin/appointments          | View all appointments              |
| POST   | /api/admin/appointments          | Create an appointment              |
| DELETE | /api/admin/appointments/{id}     | Cancel an appointment              |
| DELETE | /api/admin/appointments/{id}/delete | Delete an appointment           |
| GET    | /api/admin/pharmacies            | List all pharmacies                |
| GET    | /api/admin/pharmacies/{id}       | View specific pharmacy             |
| POST   | /api/admin/pharmacies            | Create a pharmacy                  |
| PUT    | /api/admin/pharmacies/{id}       | Update a pharmacy                  |
| GET    | /api/admin/pharmacies/search     | Search pharmacies by name          |
| DELETE | /api/admin/pharmacies/{id}       | Delete a pharmacy                  |

---

## 11. Key Java and Spring Concepts (Glossary)

### Java Basics

| Concept       | What It Is                                                    | Example                                      |
|---------------|---------------------------------------------------------------|----------------------------------------------|
| **Class**     | A blueprint for creating objects                              | `public class Patient { ... }`               |
| **Object**    | An instance of a class                                        | `Patient p = new Patient();`                 |
| **Interface** | A contract — defines methods without implementing them        | `public interface PatientRepository { ... }` |
| **Package**   | A folder that groups related classes                          | `com.labservice.model`                       |
| **Annotation**| Metadata that tells the framework what to do with a class/method | `@Entity`, `@Service`, `@GetMapping`      |
| **Generic**   | Type parameter that makes code reusable                       | `List<Patient>`, `Optional<User>`            |
| **Optional**  | A container that may or may not hold a value (avoids null)    | `Optional<Patient> p = repo.findById(1);`    |

### Spring Boot Annotations

| Annotation              | Where           | What It Does                                           |
|--------------------------|-----------------|-------------------------------------------------------|
| `@SpringBootApplication`| Main class      | Enables auto-config, component scanning, and config   |
| `@Entity`               | Model class     | Maps this class to a database table                    |
| `@Table(name="x")`      | Model class     | Sets the table name                                    |
| `@Id`                   | Model field     | Marks as primary key                                   |
| `@GeneratedValue`       | Model field     | Auto-generates values (auto-increment)                 |
| `@Column`               | Model field     | Customizes column (nullable, unique, length)           |
| `@OneToOne`             | Model field     | 1:1 relationship                                       |
| `@OneToMany`            | Model field     | 1:N relationship (one side)                            |
| `@ManyToOne`            | Model field     | N:1 relationship (many side)                           |
| `@JoinColumn`           | Model field     | Specifies the foreign key column name                  |
| `@Repository`           | Repository class| Marks as data access component                         |
| `@Service`              | Service class   | Marks as business logic component                      |
| `@RestController`       | Controller class| Handles HTTP requests, returns JSON                    |
| `@RequestMapping`       | Controller class| Sets base URL prefix — all methods inherit this path (e.g. `/api/patient`) |
| `@GetMapping`           | Controller method| Handles GET requests (read data)                      |
| `@PostMapping`          | Controller method| Handles POST requests (create data)                   |
| `@PutMapping`           | Controller method| Handles PUT requests (update data)                    |
| `@DeleteMapping`        | Controller method| Handles DELETE requests (remove data)                 |
| `@PathVariable`         | Method parameter | Extracts value from URL path (`/patients/{id}` → id) |
| `@RequestBody`          | Method parameter | Converts JSON body to Java object                      |
| `@RequestParam`         | Method parameter | Extracts value from query string (`/search?name=X` → name) |
| `@RequestHeader`        | Method parameter | Extracts HTTP header value (rare — security handles this) |
| `ResponseEntity<T>`     | Return type      | Wraps response with HTTP status code + body            |
| `RequestEntity<T>`      | Parameter type   | Wraps incoming request (exists but annotations do its job better) |
| `@Valid`                | Method parameter | Triggers validation on the object                      |
| `@Autowired`            | Field or constructor| Spring injects the dependency automatically         |
| `@Configuration`        | Config class    | Contains Spring configuration beans                    |
| `@Bean`                 | Config method   | Registers return value as a Spring-managed component   |
| `@NotNull`              | DTO field       | Validation: field cannot be null                       |
| `@NotBlank`             | DTO field       | Validation: field cannot be null or empty              |
| `@Email`                | DTO field       | Validation: must be valid email format                 |
| `@Future`               | DTO field       | Validation: date must be in the future                 |
| `@Size(min, max)`       | DTO field       | Validation: string length constraints                  |
| `@Enumerated`           | Model field     | Stores enum as STRING or ORDINAL in database           |
| `@PreAuthorize`         | Controller method| Method-level security: check role or expression       |
| `@AuthenticationPrincipal` | Method parameter | Injects the currently logged-in user's details     |
| `@EnableMethodSecurity` | Config class    | Enables @PreAuthorize annotations                      |
| `@Component`            | Any class       | Generic Spring-managed bean (filter, util, etc.)       |

### HTTP Methods (REST API)

| Method   | Purpose        | Example                           | Returns     |
|----------|---------------|-----------------------------------|-------------|
| GET      | Read data      | GET /api/patients/5               | 200 OK      |
| POST     | Create new     | POST /api/patients (with body)    | 201 Created |
| PUT      | Update existing| PUT /api/patients/5 (with body)   | 200 OK      |
| DELETE   | Remove         | DELETE /api/patients/5            | 204 No Content |

### HTTP Status Codes

| Code | Meaning              | When Used                            |
|------|----------------------|--------------------------------------|
| 200  | OK                   | Successful GET or PUT                |
| 201  | Created              | Successful POST (new resource)       |
| 204  | No Content           | Successful DELETE                    |
| 400  | Bad Request          | Invalid input (validation failed)    |
| 401  | Unauthorized         | Not logged in                        |
| 403  | Forbidden            | Logged in but not allowed            |
| 404  | Not Found            | Resource doesn't exist               |
| 500  | Internal Server Error| Bug in your code                     |

---

## Quick Start Commands

```bash
# ===== PREREQUISITES =====

# PostgreSQL must be running (check with):
pg_isready
# → /tmp:5432 - accepting connections

# ===== BACKEND (Spring Boot) =====

# Navigate to the project:
cd LabService

# Run the application:
./mvnw spring-boot:run

# The app starts at: http://localhost:8080
# Database: PostgreSQL at localhost:5432/labservicedb (data persists!)

# Default admin login:
#   Username: admin
#   Password: admin123

# Swagger API docs: http://localhost:8080/swagger-ui.html

# Query the database directly:
psql -h localhost -U meeraramesh -d labservicedb

# Run tests:
./mvnw test

# Build a JAR file:
./mvnw clean package
# The JAR will be in target/labservice-0.0.1-SNAPSHOT.jar

# Run the JAR:
java -jar target/labservice-0.0.1-SNAPSHOT.jar

# ===== FRONTEND (React/TypeScript) =====

# Navigate to frontend:
cd frontend

# Install dependencies (first time only):
npm install

# Start dev server (auto-proxies /api to Spring Boot):
npm run dev
# Opens at: http://localhost:3000

# Build for production:
npm run build
# Output in frontend/dist/

# Type-check without building:
npx tsc --noEmit
```
