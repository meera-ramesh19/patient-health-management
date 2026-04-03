# LabService — Learning Log

A step-by-step record of building the LabService project from scratch.

---

## Phase 1: Project Setup

### Step 1: Check Prerequisites

**Commands run:**
```bash
java -version
# → openjdk version "24.0.2"

javac -version
# → javac 24.0.2

mvn -version
# → Apache Maven 3.9.10
```

**What are these tools?**
- `java` — Runs compiled Java programs (the JVM — Java Virtual Machine)
- `javac` — The Java Compiler. Turns your `.java` source code into `.class` bytecode that `java` can run
- `mvn` — Maven, the build tool. Downloads libraries, compiles your code, runs tests, packages your app

---

### Step 2: Generate the Project Skeleton

**Command run:**
```bash
mvn archetype:generate \
  -DgroupId=com.labservice \
  -DartifactId=LabService \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.5 \
  -DinteractiveMode=false
```

**Breaking down the command:**
- `mvn archetype:generate` — "Maven, create a new project from a template"
- `-DgroupId=com.labservice` — Your organization name (like a domain in reverse). This becomes your base package name
- `-DartifactId=LabService` — Your project name. This becomes the folder name
- `-DarchetypeArtifactId=maven-archetype-quickstart` — The template to use (basic Java project)
- `-DinteractiveMode=false` — Don't ask me questions, just use the values I gave

**What got created:**
```
LabService/
├── pom.xml                                    # Build config + dependency list
├── src/
│   ├── main/java/com/labservice/App.java     # Main application file
│   └── test/java/com/labservice/AppTest.java # Test file
```

---

### Step 3: Understanding pom.xml

The `pom.xml` (Project Object Model) is the most important file in a Maven project. It tells Maven:
1. **Who you are** — groupId, artifactId, version
2. **What Java version** — `maven.compiler.release` = 17
3. **What libraries you need** — listed in `<dependencies>`
4. **How to build** — plugins for compiling, testing, packaging

Think of it as a recipe card:
- "This dish is called LabService" (artifactId)
- "Made by com.labservice" (groupId)
- "You'll need these ingredients" (dependencies)
- "Cook at Java 17" (compiler release)

**Current dependencies:** Only JUnit (testing). We need to add Spring Boot.

---

### Step 4: Understanding the Generated Code

**App.java** — The simplest Java program:
```java
package com.labservice;           // Package = folder location

public class App {                // Class = a blueprint/container for code
    public static void main(String[] args) {   // Entry point — Java starts HERE
        System.out.println("Hello World!");    // Print text to the terminal
    }
}
```

Key concepts:
- `package com.labservice` — Every Java file declares which package (folder) it belongs to
- `public class App` — The class name MUST match the file name (App.java = class App)
- `public static void main(String[] args)` — The magic method. Java always looks for this to start your program
  - `public` = anyone can call it
  - `static` = doesn't need an object instance
  - `void` = returns nothing
  - `String[] args` = command-line arguments

**AppTest.java** — A simple test:
```java
@Test                              // Annotation: marks this method as a test
public void shouldAnswerWithTrue() {
    assertTrue(true);              // Assert: "I expect this to be true"
}
```

---

### Step 5: Convert to a Spring Boot Project

This is where we transform from a plain Java project into a Spring Boot web application.

**What needs to change in pom.xml:**

1. Add a `<parent>` section — tells Maven "this project inherits Spring Boot's defaults"
2. Change `<packaging>` to `jar` — Spring Boot apps are packaged as runnable JAR files
3. Add Spring Boot dependencies — the libraries we need
4. Add Spring Boot Maven plugin — lets us run with `./mvnw spring-boot:run`

**Why Spring Boot?**
Without Spring Boot, you'd need to:
- Manually set up a Tomcat web server
- Manually configure database connections
- Manually wire all your classes together
- Write hundreds of lines of XML config

Spring Boot does all of this automatically with just a few annotations.

---

### Step 6: Create the Folder Structure

After converting to Spring Boot, we create the portal-based folder structure:

```
src/main/java/com/labservice/
├── LabServiceApplication.java          # Entry point (replaces App.java)
├── model/                              # Database entities
├── repository/                         # Data access
├── service/                            # Business logic
├── controller/
│   ├── auth/                           # Public endpoints (login/register)
│   ├── patient/                        # Patient portal
│   ├── doctor/                         # Doctor portal
│   └── admin/                          # Admin portal
├── dto/
│   ├── request/                        # Incoming data shapes
│   └── response/                       # Outgoing data shapes
├── security/                           # JWT + role enforcement
└── exception/                          # Error handling

src/main/resources/
├── application.properties              # App configuration
```

Each folder has ONE job — this is called "Separation of Concerns."

---

### Step 5-6: Completed

- Converted pom.xml to Spring Boot 3.4.4 with all dependencies
- Created LabServiceApplication.java with @SpringBootApplication
- Created application.properties with database, JWT, and JSON config (originally H2, later migrated to PostgreSQL)
- Created all sub-packages (model, repository, service, controller/auth/patient/doctor/admin, dto, security, exception)
- Verified: `mvn compile` → BUILD SUCCESS

---

## Phase 1.5: Model Layer (Entities)

### Concepts Learned

**What is an Entity?**
A Java class annotated with `@Entity` that maps to a database table. Each field = a column.

**Key Annotations:**
- `@Entity` + `@Table(name="x")` → marks class as a database table
- `@Id` + `@GeneratedValue` → primary key with auto-increment
- `@Column(nullable=false, unique=true)` → column constraints
- `@Enumerated(EnumType.STRING)` → stores enum as text, not number
- `@OneToOne` → 1:1 relationship (User ↔ Patient)
- `@ManyToOne` + `@JoinColumn` → N:1 relationship (many Patients → one Doctor)
- `@OneToMany(mappedBy="x")` → 1:N relationship (one Patient → many Visits)
- `@ElementCollection` → stores a simple list in a separate table
- `cascade = CascadeType.ALL` → saving parent saves children too

**Common Mistakes Made & Fixed:**
1. `package.com` instead of `package com` — dot vs space after keyword
2. `@Id@GeneratedValue` — need a space between annotations
3. Field type `Double` but getter returns `String` — types must match
4. Declaring the same field twice — Java doesn't allow duplicate field names
5. Using `String` for prescriptions/labOrders — should be `@OneToMany` relationships to separate entities
6. Missing `import java.util.List` and `import java.time.LocalDate`
7. Comment text bleeding onto next line — `// comment\ntoo` makes `too` look like code

**Rules Learned:**
- Package declaration must match folder path
- Class name must match file name
- Every entity needs an empty constructor `public X() {}`
- Every field needs a getter AND setter with matching types
- Boolean getters use `isX()` not `getX()`
- `@JoinColumn` goes on the side that holds the foreign key
- `mappedBy` goes on the side that does NOT have the foreign key

### Files Created (11 total, BUILD SUCCESS)

```
model/
├── Role.java          — Enum (ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN)
├── User.java          — Login account ↔ Patient (1:1), ↔ Doctor (1:1)
├── Address.java       — Street, city, state, zip, lat/lng
├── Doctor.java        — Profile + @OneToOne Address + @ElementCollection lists
├── Patient.java       — Profile + @ManyToOne Doctor + @ElementCollection lists
├── Visit.java         — Visit record + @OneToMany Prescription & LabOrder
├── Prescription.java  — Medication + @ManyToOne Visit & Doctor
├── LabOrder.java      — Lab test + status tracking + @ManyToOne Visit/Patient/Doctor
└── Appointment.java   — Future scheduling + @ManyToOne Patient & Doctor
```

---

## Phase 2a: Repository Layer (Data Access) — IN PROGRESS

### What is a Repository?

- **Entity** = "what does my data look like?" (done)
- **Repository** = "how do I get data in and out of the database?"

A repository is an **interface**, not a class. You define method signatures, Spring writes the implementation. You never write SQL.

### Interface vs Class

```java
// CLASS — has actual code
public class Dog {
    public void speak() { System.out.println("Woof!"); }
}

// INTERFACE — just promises "these methods will exist"
public interface Animal {
    void speak();    // no code body — someone else implements it
}
// In our case, Spring auto-generates the implementation at runtime.
```

### How Spring Generates SQL from Method Names

```
findByUsername("john")                →  SELECT * FROM users WHERE username = 'john'
findByStatus("ORDERED")              →  SELECT * FROM lab_orders WHERE status = 'ORDERED'
findByPatientIdAndStatus(1, "SCHEDULED")
                                     →  SELECT * FROM appointments
                                        WHERE patient_id = 1 AND status = 'SCHEDULED'
findByPatientIdOrderByVisitDateDesc(1)
                                     →  SELECT * FROM visits WHERE patient_id = 1
                                        ORDER BY visit_date DESC
existsByDoctorIdAndDateTime(5, dt)   →  SELECT COUNT(*) > 0 FROM appointments
                                        WHERE doctor_id = 5 AND date_time = dt
```

Naming convention: `findBy` + `FieldName` + optional `And`/`Or`/`Between`/`OrderBy`

### Free Methods from JpaRepository

Every repository that extends `JpaRepository<Entity, Long>` gets:
- `findAll()` — get all records
- `findById(id)` — get one by ID (returns Optional<T>)
- `save(entity)` — create new OR update existing
- `deleteById(id)` — delete by ID
- `count()` — count all records
- `existsById(id)` — check if exists

### Return Types

- Single result that might not exist → `Optional<Entity>`
- Multiple results → `List<Entity>`
- Yes/no check → `boolean`

### Files to Create (7 total)

```
repository/
├── UserRepository.java         — findByUsername, findByEmail, existsByUsername, existsByEmail
├── PatientRepository.java      — findByUserId, findByPrimaryDoctorId
├── DoctorRepository.java       — findBySpecialization, findByUserId
├── VisitRepository.java        — findByPatientId, findByDoctorId, findByPatientIdOrderByVisitDateDesc
├── PrescriptionRepository.java — findByVisitId, findByVisitPatientId
├── LabOrderRepository.java     — findByPatientId, findByStatus, findByPatientIdAndStatus
└── AppointmentRepository.java  — findByPatientId, findByDoctorId, existsByDoctorIdAndDateTime
```

### Common Mistakes Made & Fixed

1. **Lowercase filename** — `userRepository.java` should be `UserRepository.java`. Java requires the filename to match the class name exactly, including capitalization.
2. **Wrong import** — `import com.labservice.model.User` in PatientRepository. Each repository must import the entity it manages (`Patient`, not `User`).
3. **Method names don't match entity fields** — `findByPatientname()` fails because Patient has no `patientname` field. Method names must match actual field names: `findByUserId` works because Patient has a `user` field with an `id`.
4. **Copy-paste without adapting** — Copied UserRepository pattern for all repos. Each repository is unique — methods depend on what queries that specific entity needs.

### Files Created (7 total, BUILD SUCCESS)

```
repository/
├── UserRepository.java         ✅ findByUsername, findByEmail, existsByUsername, existsByEmail
├── PatientRepository.java      ✅ findByUserId, findByPrimaryDoctorId, findByLastNameContainingIgnoreCase
├── DoctorRepository.java       ✅ findByUserId, findBySpecialization, findByNameContainingIgnoreCase
├── VisitRepository.java        ✅ findByPatientId, findByDoctorId, OrderByVisitDateDesc, findByVisitDateBetween
├── PrescriptionRepository.java ✅ findByVisitId, findByVisitPatientId, findByEndDateAfter, findByPrescribedById
├── LabOrderRepository.java     ✅ findByPatientId, findByStatus, findByPatientIdAndStatus, findByOrderedById
└── AppointmentRepository.java  ✅ findByPatientId, findByDoctorId, existsByDoctorIdAndDateTime, findByDoctorIdAndDateTimeBetween
```

**Compiled:** 18 source files, BUILD SUCCESS

---

## Phase 3: Service Layer (Business Logic) ✅ COMPLETED

### What is a Service?

The service layer is where **business rules** live — the "chef" in the restaurant analogy.

```
Controller (waiter)  →  Service (chef)  →  Repository (pantry)
"Take the order"        "Know the recipe"    "Get ingredients"
```

**Why not put logic in the controller?**
If 3 portals (patient, doctor, admin) all deal with appointments, you'd copy the same rules 3 times. With a service, the rule exists in ONE place.

### Key Concepts Learned

**@Service annotation:**
```java
@Service    // Tells Spring: "manage this class, make it injectable"
public class AppointmentService { ... }
```

**@Autowired — Dependency Injection:**
```java
@Autowired
private AppointmentRepository appointmentRepository;
// Spring sees @Autowired, finds the matching @Repository, plugs it in
// You DON'T use "new" — Spring handles object creation
```

**Optional.orElseThrow() — Handle "not found":**
```java
Patient patient = patientRepository.findById(patientId)
    .orElseThrow(() -> new RuntimeException("Patient not found"));
// If the Optional box is empty, throw an error
// If it has a value, unwrap and return it
```

**save() does double duty:**
- New object (no ID yet) → generates `INSERT INTO ...`
- Existing object (has ID) → generates `UPDATE ... WHERE id = ?`

**A service can use MULTIPLE repositories:**
```java
// AppointmentService needs 3 repos because booking requires:
// - Check patient exists (PatientRepository)
// - Check doctor exists (DoctorRepository)
// - Save appointment (AppointmentRepository)
```

### Common Mistakes Made & Fixed

1. **Missing package/imports/@Service at top** — File started with `public class` directly. Without `package`, Java doesn't know where the class lives. Without `@Service`, Spring won't manage it and `@Autowired` won't work.
2. **String broken across lines** — `"Appointment not found with\nid: "` is invalid. Java strings must stay on one line or use `+` to concatenate.
3. **Duplicate methods** — Had both `createAppointment()` and `bookAppointment()` doing similar things. Keep the one with proper validation.

### Business Rules Implemented

| Service | Rules |
|---|---|
| AppointmentService | Can't book in past, can't double-book doctor |
| PrescriptionService | End date must be after start date |
| LabOrderService | Can't change status of completed/cancelled, auto-sets completion date |
| All services | Verify referenced entities exist before creating |

### Files Created (7 total, BUILD SUCCESS)

```
service/
├── UserService.java         ✅ enable/disable, change role, CRUD
├── PatientService.java      ✅ profile management, assign doctor, search
├── DoctorService.java       ✅ profile management, search by specialization
├── VisitService.java        ✅ record visit, history, date range queries
├── PrescriptionService.java ✅ write Rx, find active, end date validation
├── LabOrderService.java     ✅ order labs, status lifecycle, add results
└── AppointmentService.java  ✅ book with conflict detection, cancel
```

**Compiled:** 25 source files, BUILD SUCCESS

---

## Pharmacy Model Addition

### What was added

A `Pharmacy` entity to store pharmacy information and link it to patients and prescriptions.

**New files:**
- `model/Pharmacy.java` — name, phoneNumber, email, licenseNumber, @OneToOne Address, @ElementCollection hoursOfOperation and servicesOffered
- `repository/PharmacyRepository.java` — findByNameContainingIgnoreCase, findByLicenseNumber
- `service/PharmacyService.java` — CRUD, search by name

**Modified files:**
- `model/Patient.java` — added `@ManyToOne Pharmacy preferredPharmacy` with getter/setter
- `model/Prescription.java` — added `@ManyToOne Pharmacy pharmacy` with getter/setter

**How it connects:**
```
Patient ──→ Pharmacy        "My preferred pharmacy is CVS on Main St"
Prescription ──→ Pharmacy   "Send this Amoxicillin Rx to CVS on Main St"
```

**Compiled:** 28 source files, BUILD SUCCESS

### Progress Summary (at that time)

```
Layer            Files    Status
─────            ─────    ──────
Model            12       ✅ Complete
Repository       8        ✅ Complete
Service          8        ✅ Complete
Controller       0        → Next
Security         0        → Upcoming
DTO              0        → Upcoming
Exception        0        → Upcoming
```

---

## Phase 4: Controller Layer (API Endpoints) — IN PROGRESS

### What is a Controller?

The controller is the **waiter** in the restaurant analogy — it faces the customer (frontend/API caller).

```
HTTP Request → Controller (waiter) → Service (chef) → Repository (pantry) → Database
HTTP Response ← Controller ← Service ← Repository ← Database
```

A controller does THREE things:
1. **Receives** an HTTP request (GET, POST, PUT, DELETE)
2. **Delegates** to the service layer
3. **Returns** an HTTP response with a status code

### Key Annotations

| Annotation | Where | What it does |
|---|---|---|
| `@RestController` | Class | "This class handles HTTP requests and returns JSON" |
| `@RequestMapping("/api/patient")` | Class | Base URL prefix for all endpoints |
| `@GetMapping("/visits")` | Method | Handles GET requests to `/api/patient/visits` |
| `@PostMapping("/appointments")` | Method | Handles POST (create) requests |
| `@PutMapping("/appointments/{id}")` | Method | Handles PUT (update) requests |
| `@DeleteMapping("/appointments/{id}")` | Method | Handles DELETE requests |
| `@RequestBody` | Parameter | "Parse the JSON body into this Java object" |
| `@PathVariable` | Parameter | "Extract `{id}` from the URL path" |
| `ResponseEntity<T>` | Return type | Control HTTP status code (200, 201, 204, 404) |

### HTTP Methods = CRUD

```
GET    = Read      → Get my appointments
POST   = Create    → Book a new appointment
PUT    = Update    → Change appointment notes
DELETE = Delete    → Cancel an appointment
```

### @RequestMapping — URL Prefix

Saves you from repeating the same base path on every method:

```java
@RequestMapping("/api/patient")       // set once at class level
@GetMapping("/visits/{id}")           // becomes /api/patient/visits/{id}
@GetMapping("/prescriptions/{id}")    // becomes /api/patient/prescriptions/{id}
```

### ResponseEntity — Controlling the Outgoing Response

A wrapper that holds: **HTTP status code + body (your data)**

```java
return ResponseEntity.ok(data);                          // 200 OK — "Here's your data"
return ResponseEntity.status(HttpStatus.CREATED).body(x); // 201 Created — "Made a new thing"
return ResponseEntity.noContent().build();                // 204 No Content — "Done, nothing to show"
```

Without `ResponseEntity`, Spring always returns 200 OK. With it, YOU control the status code so the frontend knows what happened:

```
200 OK          → "Here's your data"
201 Created     → "I made a new thing for you"
204 No Content  → "Done, nothing to show" (deletes)
400 Bad Request → "You sent me garbage"
404 Not Found   → "That doesn't exist"
500 Server Error → "Something broke on my end"
```

### RequestEntity — Exists But Almost Never Used

`RequestEntity<T>` wraps the INCOMING request (headers + body), but annotations do its job better:

```java
// RequestEntity approach (verbose, almost nobody uses this):
public ResponseEntity<Visit> create(RequestEntity<Visit> request) {
    Visit visit = request.getBody();
    String auth = request.getHeaders().getFirst("Authorization");
}

// Annotation approach (clean, what everyone uses):
public ResponseEntity<Visit> create(
        @RequestBody Visit visit,
        @RequestHeader("Authorization") String auth) {
}
```

### Incoming vs Outgoing — Full Picture

```
INCOMING request (extracting data FROM the caller):
    @RequestBody        → "Give me the JSON body"         (used constantly)
    @PathVariable       → "Give me the {id} from the URL" (used constantly)
    @RequestParam       → "Give me the ?name=X value"     (used for search)
    @RequestHeader      → "Give me a header value"        (rare, security handles it)
    RequestEntity<T>    → "Give me everything at once"    (almost never used)

OUTGOING response (sending data BACK to the caller):
    ResponseEntity<T>   → "Here's the data + status code" (used constantly)
```

### Portal Architecture — Same Service, Different Scope

```
Patient → findByPatientId(myId)    → "Show MY appointments"
Doctor  → findByDoctorId(myId)     → "Show MY patients' appointments"
Admin   → findAll()                → "Show ALL appointments"
```

### How to Write a Controller — The Recipe

Controllers are built **directly from service methods**. You build each layer based on what the layer below offers:

```
1. Repository    → "What queries can we run?"
2. Service       → "What business operations use those queries?"
3. Controller    → "Which service methods should be exposed as URLs?"
```

For every service method, ask **3 questions:**

1. **Should this portal have access?**
   - `visitService.recordVisit()` → Doctor YES, Patient NO, Admin MAYBE
2. **What HTTP method fits?**
   - Reading = GET, Creating = POST, Changing = PUT, Removing = DELETE
3. **What inputs does the service need?**
   - Simple lookup (ID) → `@PathVariable`
   - Creating/updating (object) → `@RequestBody`, extract fields to match service params

### How Services Split Across Portals

The **same service** is used by **multiple controllers** — each portal exposes only what makes sense for that role:

```
                        PatientPortal    DoctorPortal    AdminPortal
                        ─────────────    ────────────    ───────────
visitService
  .getVisitsByPatient()     GET ✅
  .getVisitsByDoctor()                      GET ✅
  .getAllVisits()                                          GET ✅
  .recordVisit()                            POST ✅
  .updateVisit()                            PUT ✅
  .deleteVisit()                                          DELETE ✅

appointmentService
  .getByPatient()           GET ✅
  .getByDoctor()                            GET ✅
  .getAllAppointments()                                   GET ✅
  .bookAppointment()        POST ✅
  .cancelAppointment()      DELETE ✅                     DELETE ✅
```

### Common Mistakes Made & Fixed

1. **Filename ≠ class name** — `PatientController.java` contained `class PatientPortalController`. Java requires these to match exactly. Renamed file.
2. **Missing import** — Used `HttpStatus.CREATED` without importing `org.springframework.http.HttpStatus`.
3. **Missing `@Autowired` services** — Had 2 services but used 6. Every service you call needs `@Autowired` declaration.
4. **Wrong service method names** — Called `getByPatientId()` but service has `getVisitsByPatient()`. Method names must match exactly.
5. **Service parameter mismatch** — Called `bookAppointment(appointment)` but service takes `(Long, Long, LocalDateTime, String)`. Must extract fields from the request body to match the service signature.

### Files Created (4 of 4, BUILD SUCCESS)

```
controller/
├── auth/
│   └── AuthController.java            — ✅ 6 endpoints (register, login, google, social, forgot/reset password)
├── patient/
│   └── PatientPortalController.java   — ✅ 11 endpoints (mostly GET — patients read data)
├── doctor/
│   └── DoctorPortalController.java    — ✅ 14 endpoints (GET + POST/PUT — doctors create data)
└── admin/
    └── AdminPortalController.java     — ✅ 30 endpoints (full CRUD on all entities)
```

**PatientPortalController — 11 endpoints:**
- Profile: GET, PUT
- Visits: GET (read-only — doctors create visits)
- Prescriptions: GET all, GET active
- Lab Orders: GET all, GET by status
- Appointments: GET, POST (book), DELETE (cancel)
- Doctors: GET (view assigned doctor)

**DoctorPortalController — 14 endpoints:**
- Profile: GET, PUT
- My Patients: GET
- Visits: GET, POST (record visit), PUT (update diagnosis/notes)
- Prescriptions: GET, POST (write Rx)
- Lab Orders: GET, POST (order lab), PUT status, PUT results
- Appointments: GET (view schedule)

**AdminPortalController — 30 endpoints:**
- Users: GET all, GET one, PUT enable, PUT disable, PUT role, DELETE (6)
- Patients: GET all, GET one, GET search, POST, PUT, PUT assign-doctor, DELETE (7)
- Doctors: GET all, GET one, GET search, POST, PUT, DELETE (6)
- Visits: GET all, GET one, GET by date range, DELETE (4)
- Prescriptions: GET all, GET one, DELETE (3)
- Lab Orders: GET all, GET one, GET by status, DELETE (4)
- Appointments: GET all, GET one, PUT cancel, DELETE (4)
- Pharmacies: GET all, GET one, GET search, POST, PUT, DELETE (6)

### New Annotation Learned: @RequestParam

```java
// @PathVariable — value is part of the URL path
@GetMapping("/patients/{id}")         // /api/admin/patients/5
public ... getPatient(@PathVariable Long id)

// @RequestParam — value is in the query string after ?
@GetMapping("/patients/search")       // /api/admin/patients/search?name=Smith
public ... search(@RequestParam String name)
```

Use `@PathVariable` for IDs (required, part of the path). Use `@RequestParam` for search/filter values (in the query string).

### Endpoint Count by Portal

```
Portal     Endpoints   Mostly...
──────     ─────────   ─────────
Patient    11          GET (read own data)
Doctor     14          GET + POST/PUT (create medical records)
Admin      30          Full CRUD on everything
Auth       (next)      POST only (register + login)
──────     ─────────
Total      55+
```

**Compiled:** 31 source files, BUILD SUCCESS

---

## Phase 5: Security Layer + AuthController — IN PROGRESS

### Why Do We Need Security?

Right now anyone can hit any endpoint — a patient could call `/api/admin/users` and see all accounts. We need:

- **Authentication** — "WHO are you?" (login with username + password)
- **Authorization** — "WHAT are you allowed to do?" (role check)

```
Without Security:
    Anyone → GET /api/admin/users → 200 OK, here's all users 😱

With Security:
    No token     → GET /api/admin/users → 401 Unauthorized ("Who are you?")
    Patient token → GET /api/admin/users → 403 Forbidden ("You're not an admin")
    Admin token   → GET /api/admin/users → 200 OK ✅
```

### How JWT Authentication Works

JWT = **JSON Web Token** — a signed string that proves who you are.

```
Step 1: LOGIN
    User sends: POST /api/auth/login { "username": "john", "password": "secret" }
    Server checks password, generates a JWT token containing:
        { "username": "john", "role": "ROLE_PATIENT", "expires": "2025-04-01" }
    Server signs it with a secret key → sends token back

Step 2: EVERY REQUEST AFTER LOGIN
    User sends: GET /api/patient/visits
                Header: Authorization: Bearer eyJhbGciOi....(the JWT token)

    Server reads the token → verifies signature → extracts role
    If role = PATIENT and URL = /api/patient/** → ✅ Allow
    If role = PATIENT and URL = /api/admin/**   → ❌ 403 Forbidden
```

### Key Concepts

| Concept | What it is |
|---|---|
| **BCrypt** | Password hashing — stores `$2a$10$xK3j...` instead of `"password123"` |
| **JWT** | Signed token containing username + role + expiration |
| **Bearer Token** | The `Authorization: Bearer <token>` header format |
| **SecurityFilterChain** | Spring's list of rules: which URLs are public, which need roles |
| **UserDetailsService** | Spring Security's way to load a user from YOUR database |
| **OncePerRequestFilter** | Runs your code once per HTTP request (our JWT filter) |

### Files to Create (8 total)

```
security/
├── SecurityConfig.java            — "Which URLs need which roles?"
├── JwtTokenProvider.java          — "How to create and read JWT tokens"
├── JwtAuthenticationFilter.java   — "Check the token on every request"
└── CustomUserDetailsService.java  — "Load user from database for Spring Security"

controller/auth/
└── AuthController.java            — POST /api/auth/register, POST /api/auth/login

service/
└── AuthService.java               — Register new users, authenticate login

dto/request/
├── RegisterRequest.java           — { username, password, email, portalType }
└── LoginRequest.java              — { username, password }

dto/response/
└── LoginResponse.java             — { token, username, role }
```

### How They Connect — The Full Flow

**Registration:**
```
1. User sends POST /api/auth/register { username, password, email, portalType }
2. RegisterRequest (DTO) defines the JSON shape
3. AuthController receives the request
4. AuthService.register():
   - Checks username/email not taken
   - Hashes password with BCrypt ("password123" → "$2a$10$xK3j...")
   - Assigns role based on portalType ("patient" → ROLE_PATIENT)
   - Saves user to database
   - If patient, auto-creates an empty Patient profile
5. Returns the created User (201 Created)
```

**Login:**
```
1. User sends POST /api/auth/login { username, password }
2. LoginRequest (DTO) defines the JSON shape
3. AuthController receives the request
4. AuthService.login():
   - AuthenticationManager checks credentials:
     - CustomUserDetailsService loads user from database
     - BCrypt compares hashed passwords
   - If valid, JwtTokenProvider.generateToken() creates signed token
5. Returns LoginResponse { token, username, role }
```

**Every Request After Login:**
```
1. User sends GET /api/patient/visits
   Header: Authorization: Bearer eyJhbGciOi....
2. JwtAuthenticationFilter (runs before controller):
   - Extracts token from "Bearer <token>" header
   - JwtTokenProvider.validateToken() checks signature + expiry
   - Reads username + role from token
   - Tells Spring Security "this user is authenticated with this role"
3. SecurityConfig rules check: ROLE_PATIENT can access /api/patient/**? YES ✅
4. PatientPortalController handles the request normally
```

### New Concepts Learned

**DTOs (Data Transfer Objects):**
Simple data containers that define the SHAPE of JSON going in/out. Why not use the User entity directly?
- User has fields (id, enabled) the caller should NOT set
- RegisterRequest needs portalType which doesn't exist on User
- DTOs protect your entities from unwanted data

**@Component vs @Service:**
- `@Service` = business logic (AuthService, PatientService)
- `@Component` = generic Spring-managed class (JwtTokenProvider)
- Both do the same thing — `@Service` is just a more specific label

**@Value("${jwt.secret}"):**
Reads a value from `application.properties`. Like a variable that lives in a config file.

**@Configuration + @EnableWebSecurity:**
Marks a class as Spring's security setup. Contains `@Bean` methods that create Spring-managed objects.

**@Bean:**
A method annotated with `@Bean` returns an object that Spring manages. Other classes can `@Autowired` it.
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();    // Spring manages this object now
}
```

**SecurityFilterChain — The URL Rules:**
```java
.requestMatchers("/api/auth/**").permitAll()           // Public
.requestMatchers("/api/patient/**").hasAuthority("ROLE_PATIENT")
.requestMatchers("/api/doctor/**").hasAuthority("ROLE_DOCTOR")
.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
```

**OncePerRequestFilter:**
Base class for filters that run exactly once per HTTP request. Our JwtAuthenticationFilter extends it.

**SecurityContextHolder:**
A global holder for "who is the current user." After the JWT filter sets it, any code can check who's making the request.

**BCrypt — One-Way Password Hashing:**
```
"password123" → "$2a$10$xK3jN8vOqR..."    (can hash)
"$2a$10$xK3jN8vOqR..." → ???              (can NOT reverse)
```
To check login: hash the submitted password, compare to stored hash.

**data.sql:**
A SQL file in `src/main/resources/` that Spring Boot runs automatically on startup. Used to seed a default admin account.

### Files Created (9 total, BUILD SUCCESS)

```
dto/
├── request/
│   ├── RegisterRequest.java       ✅ { username, password, email, portalType }
│   └── LoginRequest.java          ✅ { username, password }
└── response/
    └── LoginResponse.java         ✅ { token, username, role }

security/
├── JwtTokenProvider.java          ✅ Generate, read, validate JWT tokens
├── CustomUserDetailsService.java  ✅ Bridge: YOUR User → Spring Security's UserDetails
├── JwtAuthenticationFilter.java   ✅ Bouncer: checks token on every request
└── SecurityConfig.java            ✅ Rule book: URL→role mapping, BCrypt, stateless sessions

service/
└── AuthService.java               ✅ Register (hash + save) and Login (check + token)

controller/auth/
└── AuthController.java            ✅ POST /register, POST /login

resources/
└── data.sql                       ✅ Seed default admin account
```

**Compiled:** 40 source files, BUILD SUCCESS

---

## Phase 5b: Data-Level Security (Only See YOUR Data) ✅ COMPLETED

### The Problem — Two Levels of Security

Security has two levels, like a hospital building:

```
LEVEL 1: DOOR BADGE (URL-level — already done in Phase 5)
══════════════════════════════════════════════════════════
SecurityConfig checks: "Does your ROLE match this URL?"

    Patient token → /api/admin/users   → ❌ 403 Forbidden
    Patient token → /api/patient/visits → ✅ Allowed

This stops a patient from accessing the doctor or admin portal.
But once inside /api/patient/**, any patient can access any patient's data.

LEVEL 2: ROOM KEY (data-level — fixed in this phase)
════════════════════════════════════════════════════════
Controller checks: "Is this YOUR data?"

    Patient A → /api/patient/visits → sees Patient A's visits ✅
    Patient A → /api/patient/visits → sees Patient B's visits ❌
```

### Why the Old Code Was Insecure

The old controller took the patient ID from the URL:

```java
// OLD — patient ID comes from the URL (the user controls it!)
@GetMapping("/visits/{patientId}")
public ResponseEntity<List<Visit>> getMyVisits(@PathVariable Long patientId) {
    return ResponseEntity.ok(visitService.getVisitsByPatient(patientId));
}

// Patient A (id=5) calls: GET /api/patient/visits/5  → sees own data ✅
// Patient A (id=5) calls: GET /api/patient/visits/9  → sees Patient B's data 😱
//                                                       (just changed the number!)
```

The problem: **the patient controls the ID**. They can type any number in the URL.

### How the Fix Works

The new controller reads the patient ID from the JWT token (the server controls it):

```java
// NEW — patient ID comes from the JWT token (the SERVER controls it!)
@GetMapping("/visits")                                // No ID in URL
public ResponseEntity<List<Visit>> getMyVisits() {
    Patient me = getLoggedInPatient();                // Read from token — can't be faked
    return ResponseEntity.ok(visitService.getVisitsByPatient(me.getId()));
}
```

### The Helper Method — `getLoggedInPatient()` / `getLoggedInDoctor()`

Both controllers have a private helper that converts a JWT token into a Patient/Doctor:

```java
private Patient getLoggedInPatient() {
    // Step 1: Read username from JWT token
    // SecurityContextHolder was set by JwtAuthenticationFilter earlier
    String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();       // "john" — from the token

    // Step 2: Find the User account
    User user = userService.getUserByUsername(username);

    // Step 3: Find the Patient profile linked to that User
    return patientService.getPatientByUserId(user.getId());
}
```

The chain: `JWT token → username → User → Patient`

Why can't someone fake this?
- The JWT token is **signed** with our secret key
- If someone changes the username inside the token, the signature won't match
- JwtAuthenticationFilter validates the signature BEFORE this code runs
- So `getAuthentication().getName()` always returns the REAL username

### What Changed in PatientPortalController

| Before (insecure) | After (secure) | Why |
|---|---|---|
| `GET /profile/{patientId}` | `GET /profile` | ID from token, not URL |
| `GET /visits/{patientId}` | `GET /visits` | ID from token, not URL |
| `GET /prescriptions/{patientId}` | `GET /prescriptions` | ID from token, not URL |
| `GET /lab-orders/{patientId}` | `GET /lab-orders` | ID from token, not URL |
| `GET /lab-orders/{patientId}/status/{status}` | `GET /lab-orders/status/{status}` | ID from token, not URL |
| `GET /appointments/{patientId}` | `GET /appointments` | ID from token, not URL |
| `GET /doctors/{patientId}` | `GET /doctors` | ID from token, not URL |
| `POST /appointments` — patient ID from body | `POST /appointments` — patient ID from token | Can't book for someone else |
| `DELETE /appointments/{id}` — no check | `DELETE /appointments/{id}` — ownership verified | Can't cancel someone else's appointment |

### What Changed in DoctorPortalController

| Before (insecure) | After (secure) | Why |
|---|---|---|
| `GET /profile/{doctorId}` | `GET /profile` | ID from token, not URL |
| `GET /patients/{doctorId}` | `GET /patients` | ID from token, not URL |
| `GET /visits/{doctorId}` | `GET /visits` | ID from token, not URL |
| `GET /prescriptions/{doctorId}` | `GET /prescriptions` | ID from token, not URL |
| `GET /lab-orders/{doctorId}` | `GET /lab-orders` | ID from token, not URL |
| `GET /appointments/{doctorId}` | `GET /appointments` | ID from token, not URL |
| `POST /visits` — doctor ID from body | `POST /visits` — doctor ID from token | Can't create visits as another doctor |
| `POST /prescriptions` — doctor ID from body | `POST /prescriptions` — doctor ID from token | Can't write Rx as another doctor |
| `POST /lab-orders` — doctor ID from body | `POST /lab-orders` — doctor ID from token | Can't order labs as another doctor |

### Key Concept: SecurityContextHolder

```java
SecurityContextHolder.getContext().getAuthentication().getName()
```

This is a **global variable** that Spring Security manages. Here's the timeline:

```
1. HTTP request arrives with "Authorization: Bearer eyJhb..."
2. JwtAuthenticationFilter runs FIRST:
   - Reads the token
   - Validates it
   - Sets SecurityContextHolder: "this user is john, role is ROLE_PATIENT"
3. SecurityConfig rules run:
   - Checks: ROLE_PATIENT can access /api/patient/**? YES ✅
4. Your controller runs:
   - Calls SecurityContextHolder.getContext().getAuthentication().getName()
   - Gets "john" — the username that was set in step 2
   - Looks up john's Patient record in the database
   - Returns ONLY john's data
```

### AdminPortalController — NOT changed

Admin endpoints still use IDs in URLs (`/api/admin/patients/5`) — this is correct because admins are supposed to see and manage everyone's data. That's their job.

### The Rule

```
Patient/Doctor controllers: ID comes from TOKEN (can't see others' data)
Admin controller:           ID comes from URL   (can see everyone's data)
```

**Compiled:** 40 source files, BUILD SUCCESS

---

## Phase 5c: Google Login (OAuth2) ✅ COMPLETED

### How Google Login Works

```
Normal Login:
    User types username + password → our server checks → returns JWT token

Google Login:
    User clicks "Sign in with Google"
    → Google popup → user logs into Google
    → Google gives a token (proof of identity)
    → our server verifies that token with Google
    → creates/finds user → returns OUR JWT token
```

The key difference: **Google handles the password**. We never see the user's Google password. We just get proof that Google verified them.

### The Full Flow

```
1. Frontend shows "Sign in with Google" button
2. User clicks → Google popup → authenticates with Google
3. Google gives the frontend a "Google ID Token" (signed proof of identity)
4. Frontend sends: POST /api/auth/google { "googleToken": "eyJ...", "portalType": "patient" }
5. AuthController receives it → calls AuthService.googleLogin()
6. AuthService:
   a. Creates a GoogleIdTokenVerifier (Google's official library)
   b. Asks Google: "Is this token real, signed by you, for our app?"
   c. Google says YES → gives us: email, name, firstName, lastName
   d. Checks our database: does this email already exist?
      - YES → load the existing user (returning user)
      - NO  → create a new account automatically:
              - Username = email (e.g., "john@gmail.com")
              - Password = random UUID (they'll never use it — always logs in via Google)
              - Role from portalType ("patient" → ROLE_PATIENT)
              - If patient, auto-create Patient profile with Google name
   e. Generate OUR JWT token (same as normal login)
7. Returns: { "token": "our-jwt...", "username": "john@gmail.com", "role": "ROLE_PATIENT" }
8. From here on, everything is IDENTICAL to normal login
   - Frontend stores our JWT token
   - Sends it with every request
   - SecurityConfig checks role
   - Controllers read user from token
```

### New Concepts Learned

**Google OAuth2 — What it is:**
OAuth2 = "let someone else handle the login." Instead of our server checking passwords, Google does it. We trust Google's answer.

**GoogleIdTokenVerifier:**
Google's official library for verifying tokens on the server side. It checks:
- Was this token signed by Google? (not forged)
- Is it for OUR app? (matches our Client ID)
- Has it expired?

```java
GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(googleClientId))  // Our Client ID
        .build();
GoogleIdToken idToken = verifier.verify(googleToken);            // Ask Google
```

**Google Client ID:**
An identifier you get from Google Cloud Console that says "this is my app." Google won't verify tokens for apps it doesn't know about. You get it by:
1. Go to https://console.cloud.google.com
2. Create a project → APIs & Services → Credentials
3. Create OAuth 2.0 Client ID → copy the ID

**Random password for Google users:**
Google users don't type a password on our site. But our User entity requires a password field (it's `nullable=false`). So we generate a random UUID and hash it with BCrypt. The user will never know or use this password — they always log in via Google.

```java
user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
```

**Find-or-create pattern:**
```java
Optional<User> existingUser = userRepository.findByEmail(email);
if (existingUser.isPresent()) {
    user = existingUser.get();       // Returning user → just log in
} else {
    user = new User();               // New user → create account
    // ... set fields, save ...
}
```

### Six Auth Endpoints Now Available

```
POST /api/auth/register         → Create account with username + password
POST /api/auth/login            → Log in with username + password → JWT
POST /api/auth/google           → Log in with Google account → JWT (dedicated endpoint)
POST /api/auth/social           → Log in with ANY social provider → JWT (unified endpoint)
POST /api/auth/forgot-password  → Request password reset (email → token saved)
POST /api/auth/reset-password   → Reset password (token + newPassword → updated)

The first four end the same way: frontend gets a JWT token.
The last two handle password recovery (no JWT involved).
```

### Unified Social Login — How It Works

```
                        POST /api/auth/social
                        { "provider": "facebook", "accessToken": "EAA..." }
                                    │
                          AuthController.socialLogin()
                                    │
                          AuthService.socialLogin()
                                    │
                    SocialAuthService.verifyAndGetUserInfo()
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
            verifyGoogle()   verifyFacebook()   verifyGitHub()
            (library call)   (API call to       (API call to
                              Facebook)          GitHub)
                    │               │               │
                    └───────────────┼───────────────┘
                                    │
                            SocialUserInfo
                         { email, firstName, lastName }
                                    │
                       Find or create user in database
                                    │
                         Generate OUR JWT token
                                    │
                        LoginResponse { token, username, role }
```

### How Each Provider Verifies

| Provider | Verification Method | Setup |
|---|---|---|
| **Google** | Library: `GoogleIdTokenVerifier.verify(token)` | Google Cloud Console → OAuth Client ID |
| **Facebook** | API: `GET graph.facebook.com/me?access_token=X` | Facebook Developers → Create App |
| **GitHub** | API: `GET api.github.com/user` with Bearer header | GitHub Settings → OAuth App |

### New Concept: RestTemplate

```java
// RestTemplate = Spring's tool for our server to call OTHER servers
// It's how we ask Facebook/GitHub: "Is this token real?"

// Facebook: token in URL
Map response = restTemplate.getForObject(
    "https://graph.facebook.com/me?access_token=" + token, Map.class);

// GitHub: token in header (more secure)
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);
HttpEntity<String> entity = new HttpEntity<>(headers);
ResponseEntity<Map> response = restTemplate.exchange(
    "https://api.github.com/user", HttpMethod.GET, entity, Map.class);
```

### New Concept: switch Expression (Java 14+)

```java
// Route to the right provider based on the name:
return switch (provider.toLowerCase()) {
    case "google"   -> verifyGoogle(accessToken);
    case "facebook" -> verifyFacebook(accessToken);
    case "github"   -> verifyGitHub(accessToken);
    default -> throw new RuntimeException("Unsupported provider: " + provider);
};
// Adding Apple/Twitter = just add another case + verification method
```

### Files Created/Modified

```
NEW:
  dto/request/GoogleLoginRequest.java   ✅ { googleToken, portalType }
  dto/request/SocialLoginRequest.java   ✅ { provider, accessToken, portalType }
  service/SocialAuthService.java        ✅ Verifies tokens for Google/Facebook/GitHub
  service/SocialUserInfo.java           ✅ Normalized user info { email, firstName, lastName }
  pom.xml                               ✅ Added google-api-client dependency
  application.properties                ✅ Added google.client-id setting

MODIFIED:
  service/AuthService.java              ✅ Added googleLogin() + socialLogin()
  controller/auth/AuthController.java   ✅ Added POST /google + POST /social
```

**Compiled:** 44 source files, BUILD SUCCESS

---

## Complete File Guide: Security Layer

### File 1: `security/JwtTokenProvider.java` — The Token Factory

**What it does:** Creates, reads, and validates JWT tokens.

**When it's used:**
- After login → `generateToken()` creates a signed token
- On every request → `validateToken()` checks if token is real
- On every request → `getUsernameFromToken()` + `getRoleFromToken()` extract user info

**Key methods:**
```java
// CREATE a token after login
String token = generateToken("john", "ROLE_PATIENT");
// → "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIi..."

// READ from a token
String username = getUsernameFromToken(token);   // → "john"
String role = getRoleFromToken(token);           // → "ROLE_PATIENT"

// VALIDATE a token
boolean valid = validateToken(token);            // → true or false
```

**How signing works:**
```
Generate: data + secret key → signed token
Validate: token + same secret key → data (or error if tampered)

If anyone changes the data inside the token, the signature won't match.
It's like a wax seal on a letter — you can see if it's been opened.
```

**Key annotations:**
- `@Component` — Spring manages this class (injectable with @Autowired)
- `@Value("${jwt.secret}")` — reads the signing key from application.properties
- `@Value("${jwt.expiration}")` — reads token lifetime (24 hours)

---

### File 2: `security/CustomUserDetailsService.java` — The Translator

**What it does:** Converts YOUR User entity into Spring Security's UserDetails format.

**Why it exists:** Spring Security has its own `UserDetails` interface. It doesn't know about your `User` entity. This class bridges the gap.

```
Your database → User { username, password, role, enabled }
                          ↓ (this class translates)
Spring Security → UserDetails { username, password, authorities, enabled }
```

**When it's used:**
- During login — `AuthenticationManager` calls `loadUserByUsername("john")`
- Spring Security uses the returned `UserDetails` to check the password

**Key concept: implements UserDetailsService**
```java
// "implements" = a promise. You're saying:
// "I PROMISE to provide a loadUserByUsername() method"
// Spring Security expects this method to exist.
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) { ... }
}
```

---

### File 3: `security/JwtAuthenticationFilter.java` — The Bouncer

**What it does:** Runs BEFORE every HTTP request. Checks for a JWT token and tells Spring Security who the user is.

**The timeline:**
```
HTTP Request arrives
     ↓
[JwtAuthenticationFilter] ← THIS FILE
  1. Look for "Authorization: Bearer <token>" header
  2. If found → validate token → read username + role
  3. Tell Spring Security: "this is john, he's a PATIENT"
     ↓
[SecurityConfig rules]
  4. Check: can ROLE_PATIENT access this URL?
     ↓
[Your Controller]
  5. Handle the request
```

**Key concept: extends OncePerRequestFilter**
```java
// "extends" = inherits behavior from a parent class.
// OncePerRequestFilter guarantees this code runs exactly ONCE per request.
// Without it, if the request gets forwarded internally, the filter might run twice.
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) { ... }
}
```

**Key concept: SecurityContextHolder**
```java
// After validating the token, we "stamp the user's hand":
SecurityContextHolder.getContext().setAuthentication(authentication);
// Now ANY code in this request can check who the user is.
// Controllers use this to get the logged-in patient/doctor.
```

---

### File 4: `security/SecurityConfig.java` — The Rule Book

**What it does:** THE master configuration. Answers three questions:
1. Which URLs are public?
2. Which URLs need which roles?
3. How do we handle passwords and tokens?

**The URL rules (the most important part):**
```java
.requestMatchers("/api/auth/**").permitAll()           // Anyone
.requestMatchers("/h2-console/**").permitAll()          // Anyone
.requestMatchers("/api/patient/**").hasAuthority("ROLE_PATIENT")
.requestMatchers("/api/doctor/**").hasAuthority("ROLE_DOCTOR")
.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
.anyRequest().authenticated()                           // All else: must be logged in
```

**Three @Bean methods:**
```java
@Bean SecurityFilterChain    → the URL rules above
@Bean PasswordEncoder        → BCryptPasswordEncoder (hashes passwords)
@Bean AuthenticationManager  → checks username + password (used by AuthService.login())
```

**Key concepts:**
- `@Configuration` — "this class contains Spring setup code"
- `@EnableWebSecurity` — "turn on Spring Security"
- `.csrf().disable()` — CSRF protection is for browser forms, not REST APIs with JWT
- `.sessionManagement(STATELESS)` — no server-side sessions; JWT IS the session
- `.addFilterBefore()` — register our JwtAuthenticationFilter

---

## Complete File Guide: Service Layer

### File 1: `service/UserService.java` — Account Management

**Used by:** AdminPortalController
**Methods:** getAllUsers, getUserById, getUserByUsername, enableUser, disableUser, changeRole, deleteUser

**What it does:** CRUD operations on user accounts. Admin enables/disables accounts, changes roles. Also used by controllers to look up the logged-in user via `getUserByUsername()`.

---

### File 2: `service/PatientService.java` — Patient Profiles

**Used by:** PatientPortalController, DoctorPortalController, AdminPortalController, AuthService
**Methods:** getAllPatients, getPatientById, getPatientByUserId, getPatientsByDoctor, searchByLastName, createPatient, updatePatient, assignDoctor, deletePatient

**What it does:** Manages patient profiles. AuthService calls `createPatient()` during registration. PatientPortalController calls `getPatientByUserId()` via `getLoggedInPatient()`.

---

### File 3: `service/DoctorService.java` — Doctor Profiles

**Used by:** DoctorPortalController, AdminPortalController
**Methods:** getAllDoctors, getDoctorById, getDoctorByUserId, getDoctorsBySpecialization, searchByName, createDoctor, updateDoctor, deleteDoctor

**What it does:** Manages doctor profiles. Same pattern as PatientService.

---

### File 4: `service/VisitService.java` — Visit Records

**Used by:** DoctorPortalController (create/update), PatientPortalController (read), AdminPortalController (read/delete)
**Methods:** getAllVisits, getVisitById, getVisitsByPatient, getVisitsByPatientNewestFirst, getVisitsByDoctor, getVisitsByDateRange, recordVisit, updateVisit, deleteVisit

**What it does:** Doctors create visits with `recordVisit()` (takes 7 params — patientId, doctorId, date, reason, diagnosis, notes, type). Patients view their visits. Admins view all and can delete.

---

### File 5: `service/PrescriptionService.java` — Medication Management

**Used by:** DoctorPortalController (create), PatientPortalController (read), AdminPortalController (read/delete)
**Methods:** getAllPrescriptions, getPrescriptionById, getPrescriptionsByVisit, getPrescriptionsByPatient, getActivePrescriptions, getPrescriptionsByDoctor, createPrescription, updatePrescription, deletePrescription

**Business rule:** End date must be after start date.

---

### File 6: `service/LabOrderService.java` — Lab Test Management

**Used by:** DoctorPortalController (create/update status/add results), PatientPortalController (read), AdminPortalController (read/delete)
**Methods:** getAllLabOrders, getLabOrderById, getLabOrdersByPatient, getLabOrdersByStatus, getLabOrdersByPatientAndStatus, getLabOrdersByDoctor, createLabOrder, updateStatus, addResults, deleteLabOrder

**Business rules:**
- Can't change status of COMPLETED or CANCELLED orders
- Auto-sets completion date when status changes to COMPLETED
- Status flow: ORDERED → IN_PROGRESS → COMPLETED or CANCELLED

---

### File 7: `service/AppointmentService.java` — Scheduling

**Used by:** PatientPortalController (book/cancel), DoctorPortalController (view schedule), AdminPortalController (view/cancel/delete)
**Methods:** getAllAppointments, getAppointmentById, getAppointmentsByPatient, getAppointmentsByDoctor, bookAppointment, cancelAppointment, deleteAppointment

**Business rules:**
- Can't book in the past
- Can't double-book a doctor (same time slot)

---

### File 8: `service/PharmacyService.java` — Pharmacy Management

**Used by:** AdminPortalController
**Methods:** getAllPharmacies, getPharmacyById, searchByName, createPharmacy, updatePharmacy, deletePharmacy

---

### File 9: `service/AuthService.java` — Authentication

**Used by:** AuthController
**Methods:** register, login, googleLogin, socialLogin

**What it does:**
- `register()` — hash password + assign role + save user + auto-create patient profile
- `login()` — check credentials via AuthenticationManager + generate JWT
- `googleLogin()` — verify Google token + find/create user + generate JWT
- `socialLogin()` — delegates to SocialAuthService for any provider + find/create user + generate JWT

---

### File 10: `service/SocialAuthService.java` — Social Provider Verification

**Used by:** AuthService.socialLogin()
**Methods:** verifyAndGetUserInfo (routes to verifyGoogle, verifyFacebook, verifyGitHub)

**What it does:** Knows how to talk to each social provider. Returns normalized `SocialUserInfo { email, firstName, lastName }` regardless of which provider was used.

**Key tool: RestTemplate** — Spring's HTTP client for calling external APIs (Facebook, GitHub).

---

### File 11: `service/SocialUserInfo.java` — Social User Data Container

**Used by:** SocialAuthService, AuthService
**Fields:** email, firstName, lastName

**What it does:** Simple container that normalizes user info from all providers into one shape. Google, Facebook, and GitHub all return data differently — this class makes them look the same.

---

## How Security and Service Files Work Together

```
HTTP Request with "Authorization: Bearer <token>"
     │
     ▼
security/JwtAuthenticationFilter.java     ← reads token, validates, sets user
     │ uses
     ▼
security/JwtTokenProvider.java            ← validates signature, extracts username+role
     │
     ▼
security/SecurityConfig.java              ← checks: does role match URL?
     │
     ▼
controller/                               ← calls getLoggedInPatient()/getLoggedInDoctor()
     │ uses
     ▼
service/UserService.java                  ← getUserByUsername() to find logged-in user
     │
     ▼
service/PatientService.java               ← getPatientByUserId() to find patient profile
(or DoctorService)
     │
     ▼
service/VisitService.java (etc.)          ← getVisitsByPatient() with the CORRECT patient ID
     │
     ▼
repository/                               ← database query
     │
     ▼
Response back to user (only THEIR data)
```

---

## Forgot Password & Reset Password

### The Problem
Users forget passwords. We need a secure way to let them set a new one without knowing the old one.

### The Solution: Token-Based Password Reset

```
┌─────────────────────────────────────────────────────────────────┐
│  FORGOT PASSWORD FLOW                                            │
│                                                                   │
│  User: "I forgot my password"                                    │
│     │                                                             │
│     ▼                                                             │
│  POST /api/auth/forgot-password { "email": "john@mail.com" }    │
│     │                                                             │
│     ▼                                                             │
│  AuthService.forgotPassword()                                    │
│     │                                                             │
│     ├─ Find user by email                                        │
│     ├─ Generate random UUID token: "550e8400-e29b-41d4-..."     │
│     ├─ Set resetTokenExpiry = now + 30 minutes                   │
│     ├─ Save both to user record                                  │
│     └─ (In production: email the token as a link)                │
│     │                                                             │
│     ▼                                                             │
│  Response: "If that email is registered, a reset link was sent." │
│  (ALWAYS this message, even if email doesn't exist!)             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  RESET PASSWORD FLOW                                             │
│                                                                   │
│  User clicks link in email → enters new password                 │
│     │                                                             │
│     ▼                                                             │
│  POST /api/auth/reset-password                                   │
│  { "token": "550e8400-...", "newPassword": "mynewpassword" }    │
│     │                                                             │
│     ▼                                                             │
│  AuthService.resetPassword()                                     │
│     │                                                             │
│     ├─ Find user by resetToken (findByResetToken)                │
│     ├─ Check: is resetTokenExpiry BEFORE now? → EXPIRED!         │
│     ├─ Hash new password with BCrypt                              │
│     ├─ Save new password                                          │
│     └─ Clear resetToken + resetTokenExpiry (one-time use!)       │
│     │                                                             │
│     ▼                                                             │
│  Response: "Password has been reset successfully."               │
└─────────────────────────────────────────────────────────────────┘
```

### New Concepts Learned

**UUID (Universally Unique Identifier):**
```java
String token = UUID.randomUUID().toString();
// → "550e8400-e29b-41d4-a716-446655440000"
// Every call generates a DIFFERENT string. Practically impossible to guess.
// Perfect for reset tokens — random, unique, unguessable.
```

**LocalDateTime — Working with Time:**
```java
// Set expiry to 30 minutes from now
user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

// Check if token has expired
if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
    // Token has expired! The time stored is BEFORE right now.
    throw new RuntimeException("Reset token has expired");
}
```

**User Enumeration Prevention:**
```java
// BAD — reveals which emails are registered:
if (user not found) return "Email not found";     // Attacker learns: not registered
if (user found) return "Reset link sent";          // Attacker learns: IS registered

// GOOD — reveals nothing:
return "If that email is registered, a reset link has been sent.";
// Attacker can't tell the difference between registered and unregistered emails
```

**One-Time Use Tokens:**
```java
// After resetting the password, CLEAR the token
user.setResetToken(null);
user.setResetTokenExpiry(null);
userRepository.save(user);
// Now if someone tries to use the same link again → "Invalid reset token"
```

### Why There's No Logout Endpoint

```
TRADITIONAL APPS (sessions):                    JWT APPS (our approach):
─────────────────────────                       ────────────────────────
Server stores sessions in memory                Server stores NOTHING
  Session ID → { user: "john", role: "..." }     Token contains everything

Login: server creates session                   Login: server creates token
  → gives you session ID (cookie)                → gives you JWT (string)

Every request: server looks up session          Every request: server reads token
  "Is session #abc123 valid?"                     "Is this token's signature valid?"

Logout: server DELETES the session              Logout: ??? nothing to delete!
  Session gone → you're logged out                Token still valid until it expires

                                                 Solution: FRONTEND deletes the token
                                                 localStorage.removeItem("token");
                                                 → Can't make requests → logged out
```

**If a token is stolen:** It works until it expires. Mitigated by:
1. Short expiry times (15-30 minutes for sensitive apps)
2. Token blacklist in Redis/database (adds complexity, defeats statelessness)

For our app, short expiry + HTTPS is sufficient.

### Files Created/Modified

```
NEW:
  dto/request/ForgotPasswordRequest.java  ✅ { email }
  dto/request/ResetPasswordRequest.java   ✅ { token, newPassword }

MODIFIED:
  model/User.java                         ✅ Added resetToken + resetTokenExpiry fields
  repository/UserRepository.java          ✅ Added findByResetToken() query
  service/AuthService.java                ✅ Added forgotPassword() + resetPassword()
  controller/auth/AuthController.java     ✅ Added POST /forgot-password + POST /reset-password
```

**Compiled:** BUILD SUCCESS

---

---

## Exception Handling — The Safety Net

### The Problem

Before this, every error in our app was `throw new RuntimeException("...")`. This caused two big problems:

1. **Every error returned HTTP 500** (Internal Server Error) — even when the real problem was "patient not found" (should be 404) or "username taken" (should be 409)
2. **The frontend got ugly responses** — raw stack traces or inconsistent error formats

### The Solution: Custom Exceptions + Global Handler

We built 6 files in the `exception/` package:

```
exception/
├── ResourceNotFoundException.java    → HTTP 404 (thing doesn't exist)
├── BadRequestException.java          → HTTP 400 (invalid data / business rule broken)
├── DuplicateResourceException.java   → HTTP 409 (already exists)
├── UnauthorizedException.java        → HTTP 401 (not yours / invalid token)
├── ErrorResponse.java                → The JSON shape all errors return
└── GlobalExceptionHandler.java       → Catches exceptions from ALL controllers
```

### How It Works — The Full Picture

```
                        Controller method runs
                                │
                                ▼
                        Service method throws exception
                        throw new ResourceNotFoundException("Patient not found with id: 5")
                                │
                                ▼
                    ┌───────────────────────────────┐
                    │   GlobalExceptionHandler      │
                    │   (@ControllerAdvice)          │
                    │                               │
                    │   Catches the exception type:  │
                    │   ResourceNotFoundException   │
                    │        → @ExceptionHandler     │
                    │        → returns 404 + JSON    │
                    └───────────────────────────────┘
                                │
                                ▼
                        HTTP Response:
                        Status: 404 Not Found
                        Body: {
                          "status": 404,
                          "error": "Not Found",
                          "message": "Patient not found with id: 5",
                          "timestamp": "2026-03-15T14:30:00"
                        }
```

### New Concept: @ControllerAdvice

```java
@ControllerAdvice    // ← "Apply this class to ALL controllers"
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)   // ← "When THIS is thrown..."
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        // ...return 404 with clean JSON                 // ← "...run THIS method"
    }
}
```

Think of `@ControllerAdvice` like a safety net under a trapeze:
- The trapeze artists (controllers) do their work up high
- If anyone falls (throws an exception), the net catches them
- The net (GlobalExceptionHandler) decides how to handle the fall cleanly

Without the net: person hits the ground → ugly crash (HTTP 500 + stack trace)
With the net: person is caught safely → clean landing (proper HTTP status + JSON)

### New Concept: Custom Exception Classes

```java
// Each custom exception is just a RuntimeException with a specific name.
// The NAME is what matters — GlobalExceptionHandler uses it to decide
// which HTTP status code to return.

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);   // Pass the message to RuntimeException
    }
}

// That's it! The class is tiny. Its only job is to be NAMED differently
// so GlobalExceptionHandler can tell exceptions apart.
```

**Why `extends RuntimeException`?**
- `RuntimeException` = "unchecked" exception → you don't have to declare it in method signatures
- If we used `Exception` (checked), every method would need `throws ResourceNotFoundException`
- Unchecked keeps the code clean — Spring handles it behind the scenes

### New Concept: ErrorResponse — Consistent Error Shape

```java
// Before: frontend received random strings, HTML pages, or stack traces
// After:  frontend ALWAYS receives this exact JSON shape

{
    "status": 404,                              // HTTP status code (number)
    "error": "Not Found",                       // Short label (string)
    "message": "Patient not found with id: 5",  // Detailed message (string)
    "timestamp": "2026-03-15T14:30:00"         // When it happened (date)
}

// The frontend can now reliably do:
//   if (error.status === 404) show "Not found" page
//   if (error.status === 409) show "Already exists" message
//   alert(error.message)  ← always works, always has a useful message
```

### Which Exception Goes Where

```
EXCEPTION                      HTTP CODE    WHEN TO USE
──────────────────────────     ─────────    ──────────────────────────────────
ResourceNotFoundException      404          .findById() returns empty
                                            "Patient not found with id: 5"
                                            "Doctor not found"
                                            "Visit not found"

BadRequestException            400          Business rule broken
                                            "Cannot book appointments in the past"
                                            "End date cannot be before start date"
                                            "Cannot change status of a COMPLETED lab order"
                                            "Unsupported social provider: twitter"
                                            "Reset token has expired"

DuplicateResourceException     409          Trying to create something that exists
                                            "Username already taken: john"
                                            "Email already registered: john@mail.com"

UnauthorizedException          401          Not your data / invalid credentials
                                            "You can only cancel your own appointments"
                                            "Invalid Google token"
                                            "Invalid reset token"

Exception (catch-all)          500          Anything unexpected
                                            Returns generic message (hides internals)
```

### The Catch-All Handler — Why It Matters

```java
@ExceptionHandler(Exception.class)     // Catches EVERYTHING else
public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    ex.printStackTrace();              // Log the real error (for developers)

    return new ErrorResponse(
        500,
        "Internal Server Error",
        "Something went wrong. Please try again later."   // Generic message
    );
}
```

**Why generic message?** If we showed the real error, it could leak:
- Database table names → attacker knows your schema
- File paths → attacker knows your server structure
- SQL errors → attacker can craft SQL injection attacks

The real error is logged to the server console (for developers). The user only sees a safe, generic message.

### Files Modified — RuntimeException → Custom Exception

```
Service                     Exception Types Used
───────                     ───────────────────
UserService.java            ResourceNotFoundException
PatientService.java         ResourceNotFoundException
DoctorService.java          ResourceNotFoundException
PharmacyService.java        ResourceNotFoundException
VisitService.java           ResourceNotFoundException
PrescriptionService.java    ResourceNotFoundException + BadRequestException
LabOrderService.java        ResourceNotFoundException + BadRequestException
AppointmentService.java     ResourceNotFoundException + BadRequestException
AuthService.java            DuplicateResourceException + UnauthorizedException
                            + ResourceNotFoundException + BadRequestException
SocialAuthService.java      UnauthorizedException + BadRequestException
PatientPortalController.java UnauthorizedException
```

**Compiled:** BUILD SUCCESS

---

---

## Email Service

### The Problem
The forgot password feature was using `System.out.println()` to log the reset token. In a real app, users need to receive an email with the reset link.

### The Solution: Spring Boot Mail

```
┌──────────────────────────────────────────────────────┐
│  HOW EMAIL SENDING WORKS                              │
│                                                        │
│  1. User clicks "Forgot Password"                     │
│  2. AuthService generates reset token                  │
│  3. AuthService calls emailService.sendPasswordResetEmail() │
│  4. EmailService creates a SimpleMailMessage:          │
│       To: john@mail.com                               │
│       Subject: LabService — Reset Your Password       │
│       Body: Click this link: http://localhost:3000/    │
│             reset-password?token=abc123               │
│  5. JavaMailSender sends it via SMTP (e.g., Gmail)    │
│  6. User receives email → clicks link → resets password│
└──────────────────────────────────────────────────────┘
```

### New Concepts

**JavaMailSender** — Spring's email-sending tool. Auto-configured from `application.properties`:
```properties
spring.mail.host=smtp.gmail.com       # Which email server to use
spring.mail.port=587                   # Port for TLS encryption
spring.mail.username=you@gmail.com     # Your email address
spring.mail.password=YOUR_APP_PASSWORD # App password (not your regular password!)
```

**SimpleMailMessage** — a plain text email:
```java
SimpleMailMessage message = new SimpleMailMessage();
message.setFrom("noreply@labservice.com");
message.setTo("john@mail.com");
message.setSubject("Reset Your Password");
message.setText("Click here: http://...");
mailSender.send(message);
```

**Why a separate EmailService?**
- AuthService shouldn't know HOW to send emails (separation of concerns)
- Easy to swap providers later (Gmail → SendGrid → AWS SES)
- Easy to mock in tests (just mock EmailService, don't send real emails)

---

## Dashboard Endpoints

### What Dashboards Show

Each portal has a dashboard — a summary page with key stats:

```
PATIENT DASHBOARD:                    DOCTOR DASHBOARD:
──────────────────                    ─────────────────
{                                     {
  "upcomingAppointments": 2,            "totalPatients": 45,
  "activePrescriptions": 3,            "upcomingAppointments": 8,
  "pendingLabOrders": 1,               "pendingLabOrders": 3,
  "nextAppointment": { ... },          "todaysAppointments": [ ... ],
  "currentMedications": [ ... ],       "pendingLabs": [ ... ]
  "recentLabOrders": [ ... ]         }
}

ADMIN DASHBOARD:
────────────────
{
  "totalUsers": 150,
  "totalPatients": 120,
  "totalDoctors": 25,
  "totalAppointments": 500,
  "totalVisits": 300,
  "totalLabOrders": 200,
  "pendingLabOrders": 15,
  "completedLabOrders": 180,
  "totalPrescriptions": 400,
  "totalPharmacies": 10
}
```

### New Concept: Java Stream API

```java
// Streams let you filter, sort, and transform lists in one chain

// Find the next upcoming appointment (soonest future one):
Appointment next = appointments.stream()        // start streaming the list
    .filter(a -> a.getDateTime().isAfter(now))  // keep only future ones
    .min((a, b) -> a.getDateTime().compareTo(b.getDateTime()))  // find earliest
    .orElse(null);                               // if none found, return null

// Get active prescriptions (not expired):
List<Prescription> active = prescriptions.stream()
    .filter(p -> p.getEndDate() == null || p.getEndDate().isAfter(today))
    .collect(Collectors.toList());               // collect back into a List

// Get last 5 lab orders, newest first:
List<LabOrder> recent = labs.stream()
    .sorted((a, b) -> b.getOrderedDate().compareTo(a.getOrderedDate()))
    .limit(5)
    .collect(Collectors.toList());
```

**Stream methods cheat sheet:**
```
.stream()     → start streaming a list
.filter()     → keep items that match a condition
.map()        → transform each item
.sorted()     → sort items
.limit(n)     → take only the first n items
.min() / .max() → find smallest/largest by comparator
.collect(Collectors.toList()) → collect results back into a List
.count()      → count matching items
```

---

## Swagger / OpenAPI Documentation

### What Swagger Does

Swagger auto-generates a live web page that documents your entire API:
- Every endpoint listed with HTTP method, URL, parameters, and body shape
- "Try it out" button to test endpoints right in the browser
- "Authorize" button to paste your JWT token for testing protected endpoints

```
Visit: http://localhost:8080/swagger-ui.html

What you see:
┌─────────────────────────────────────────────────────────┐
│  LabService API  v1.0.0                    [Authorize]  │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ▸ auth-controller         (6 endpoints)                │
│  ▸ patient-portal-controller (12 endpoints)             │
│  ▸ doctor-portal-controller  (14 endpoints)             │
│  ▸ admin-portal-controller   (31+ endpoints)            │
│                                                          │
│  Click any endpoint → see parameters → Try it out       │
└─────────────────────────────────────────────────────────┘
```

### How It Works

**Zero code changes to your controllers!** Swagger reads your existing annotations:
```
@GetMapping("/profile")  →  Swagger shows: GET /api/patient/profile
@PostMapping("/visits")  →  Swagger shows: POST /api/doctor/visits
@RequestBody Visit       →  Swagger shows the Visit JSON shape
@PathVariable Long id    →  Swagger shows the id parameter
```

**SwaggerConfig.java** customizes the page:
```java
@Bean
public OpenAPI labServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("LabService API").version("1.0.0"))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components().addSecuritySchemes("Bearer Authentication",
            new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("bearer").bearerFormat("JWT")));
}
```

**SecurityConfig.java** allows public access to Swagger pages:
```java
.requestMatchers("/swagger-ui/**").permitAll()
.requestMatchers("/v3/api-docs/**").permitAll()
```

---

## JUnit Tests

### What Unit Tests Do

Unit tests verify that your code works correctly WITHOUT running the full app. Each test:
1. **Arranges** test data (sets up inputs)
2. **Acts** (calls the method being tested)
3. **Asserts** (checks the result is correct)

```
TEST: "Can a patient book an appointment in the past?"
  Arrange: date = yesterday
  Act:     appointmentService.bookAppointment(..., yesterday, ...)
  Assert:  BadRequestException is thrown with message "Cannot book appointments in the past"
  ✅ PASS
```

### Key Testing Concepts

**@Mock — Fake Dependencies:**
```java
@Mock
private UserRepository userRepository;  // FAKE — doesn't hit the database

// Tell the fake what to return:
when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

// Now when UserService calls userRepository.findById(1L),
// it gets testUser back — no database needed!
```

**@InjectMocks — Real Service with Fake Dependencies:**
```java
@InjectMocks
private UserService userService;  // REAL service, but uses the @Mock repository

// userService.getUserById(1L) works normally,
// but talks to the FAKE repository instead of the real database
```

**Common Assert Methods:**
```java
assertEquals(expected, actual)        // values must be equal
assertTrue(condition)                  // condition must be true
assertFalse(condition)                 // condition must be false
assertNull(value)                      // value must be null
assertNotNull(value)                   // value must NOT be null
assertThrows(ExceptionClass.class,     // must throw this exception
    () -> methodThatShouldThrow());
```

**verify() — Check That Something Happened:**
```java
verify(userRepository).save(testUser);           // save() was called with testUser
verify(emailService, never()).sendPasswordResetEmail(any(), any());  // email was NOT sent
verify(userRepository).deleteById(1L);           // deleteById was called with 1
```

### Test Files Created

```
src/test/java/com/labservice/service/
├── UserServiceTest.java          — 9 tests
│   ├── getAllUsers returns list
│   ├── getUserById existing → returns user
│   ├── getUserById missing → throws ResourceNotFoundException
│   ├── getUserByUsername existing → returns user
│   ├── getUserByUsername missing → throws ResourceNotFoundException
│   ├── enableUser sets enabled=true
│   ├── disableUser sets enabled=false
│   ├── changeRole updates role
│   └── deleteUser calls deleteById
│
├── AppointmentServiceTest.java   — 7 tests
│   ├── bookAppointment valid → creates with SCHEDULED status
│   ├── bookAppointment past date → throws BadRequestException
│   ├── bookAppointment double-booked → throws BadRequestException
│   ├── bookAppointment patient not found → throws ResourceNotFoundException
│   ├── bookAppointment doctor not found → throws ResourceNotFoundException
│   ├── cancelAppointment → sets status to CANCELLED
│   └── getAppointmentById missing → throws ResourceNotFoundException
│
├── AuthServiceTest.java          — 9 tests
│   ├── register valid → creates user with ROLE_PATIENT
│   ├── register duplicate username → throws DuplicateResourceException
│   ├── register duplicate email → throws DuplicateResourceException
│   ├── register doctor type → sets ROLE_DOCTOR and disabled
│   ├── forgotPassword existing email → generates token, sends email
│   ├── forgotPassword unknown email → returns same message, no email sent
│   ├── resetPassword valid token → updates password, clears token
│   ├── resetPassword expired → throws BadRequestException
│   └── resetPassword invalid → throws UnauthorizedException
│
└── LabOrderServiceTest.java      — 6 tests
    ├── updateStatus ORDERED→IN_PROGRESS → succeeds
    ├── updateStatus COMPLETED → throws BadRequestException
    ├── updateStatus CANCELLED → throws BadRequestException
    ├── addResults → sets results + status COMPLETED + completion date
    ├── getLabOrderById missing → throws ResourceNotFoundException
    └── createLabOrder valid → creates with ORDERED status
```

**Test results:** 31 tests, 0 failures, BUILD SUCCESS

---

### Progress Summary

```
Layer            Files    Status
─────            ─────    ──────
Model            12       ✅ Complete
Repository       8        ✅ Complete
Service          13       ✅ Complete (added EmailService, DashboardService)
Controller       4        ✅ Complete (added dashboard endpoints to each portal)
Security         4        ✅ Complete
Config           1        ✅ Complete (SwaggerConfig)
Data Security    —        ✅ Complete (patient/doctor controllers use token for ID)
DTO              10       ✅ Complete (added PatientDashboard, DoctorDashboard, AdminDashboard)
Exception        6        ✅ Complete
Tests            4        ✅ Complete (31 tests, 0 failures)

Total: 62 source files + 4 test files = 66 Java files
Frontend       34       ✅ Complete (React/TypeScript — 100% endpoint coverage)
Database       —        ✅ PostgreSQL (persistent, data survives restarts)
```

---

## Phase 3: React/TypeScript Frontend

### What We Built

A complete React frontend that connects to the Spring Boot backend, with separate interfaces for each portal (Patient, Doctor, Admin).

### Step 1: Project Setup

**Commands run:**
```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install react-router-dom axios
```

**What these tools do:**
- **Vite** — modern build tool that replaces Create React App. Much faster because it uses native ES modules during development instead of bundling everything
- **react-ts template** — sets up a React project with TypeScript pre-configured
- **react-router-dom** — adds page-based routing (URLs like `/patient/dashboard`)
- **axios** — HTTP client for making API calls to the Spring Boot backend

**What TypeScript adds to JavaScript:**
```typescript
// JavaScript — no type safety, errors at runtime:
function login(data) { ... }  // what is "data"? could be anything

// TypeScript — errors caught at compile time:
interface LoginRequest { username: string; password: string; }
function login(data: LoginRequest) { ... }  // must have username + password
```

---

### Step 2: API Layer (5 files)

**The HTTP client pattern (client.ts):**
```typescript
import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

// Request interceptor — runs BEFORE every API call
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor — runs AFTER every API response
api.interceptors.response.use(
  (response) => response,           // success → pass through
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();          // token expired
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

**Why this matters:** Every API call automatically gets the JWT token attached. If the token expires (401), the user is automatically redirected to login. Individual pages don't need to worry about auth headers.

**API module pattern (auth.ts, patient.ts, doctor.ts, admin.ts):**
```typescript
export const patientApi = {
  getDashboard: () => api.get('/patient/dashboard'),
  getAppointments: () => api.get('/patient/appointments'),
  bookAppointment: (data: any) => api.post('/patient/appointments', data),
  // ... more methods
};
```

Each portal gets its own API file. Components import only what they need: `import { patientApi } from '../../api/patient'`.

---

### Step 3: Auth Context (global state)

**The problem:** Multiple components need to know if the user is logged in and what role they have. Without global state, you'd have to pass `user` as a prop through every component.

**The solution — React Context:**
```typescript
// AuthContext.tsx — provides auth state to the entire app
const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const login = async (data) => {
    const response = await authApi.login(data);
    localStorage.setItem('token', response.data.token);
    setUser(response.data);
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

// Any component can now do:
const { user, login, logout, isAuthenticated } = useAuth();
```

**How it wraps the app (main.tsx):**
```typescript
<BrowserRouter>       {/* enables routing */}
  <AuthProvider>      {/* provides auth state */}
    <App />           {/* the actual app */}
  </AuthProvider>
</BrowserRouter>
```

---

### Step 4: Protected Routes

**ProtectedRoute component:**
```typescript
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" />;
  if (requiredRole && user?.role !== requiredRole) return <Navigate to="/" />;
  return <>{children}</>;
}
```

**How it's used in App.tsx:**
```typescript
<Route path="/patient/dashboard"
  element={
    <ProtectedRoute requiredRole="ROLE_PATIENT">
      <PatientDashboard />
    </ProtectedRoute>
  }
/>
```

If a doctor tries to visit `/patient/dashboard`, they get redirected to `/`. If a guest tries, they get redirected to `/login`.

---

### Step 5: Page Components (20 pages)

**Common pattern used in every page:**
```typescript
export default function PatientDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patientApi.getDashboard()
      .then(res => setData(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);  // [] = run once on mount

  if (loading) return <div>Loading...</div>;
  return <div>... render data ...</div>;
}
```

**Key React concepts used:**
- `useState` — stores data in the component (re-renders when changed)
- `useEffect` — runs code on mount (like componentDidMount) for fetching data
- `useNavigate` — programmatic navigation (e.g., redirect after login)
- Conditional rendering: `{error && <div>{error}</div>}` — only shows if error is truthy

**Pages created:**
| Portal  | Pages | Features |
|---------|-------|----------|
| Auth    | 4     | Login, Register, Forgot Password, Reset Password |
| Patient | 6     | Dashboard, Appointments (book/cancel), Visits, Prescriptions, Lab Orders, Profile (view/edit) |
| Doctor  | 6     | Dashboard, Patients, Appointments, Visits (record), Prescriptions (write), Lab Orders (order/status/results) |
| Admin   | 5     | Dashboard (10 metrics), Users (enable/disable/delete), Patients, Doctors, Appointments |

---

### Step 6: Vite Proxy (connecting frontend to backend)

**The problem:** React runs on port 3000, Spring Boot on port 8080. Browsers block cross-origin requests (CORS).

**The solution (vite.config.ts):**
```typescript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

**How it works:**
```
Browser request: GET http://localhost:3000/api/patient/dashboard
Vite intercepts: "starts with /api, forward to 8080"
Vite forwards:   GET http://localhost:8080/api/patient/dashboard
Spring responds:  { upcomingAppointments: 3, ... }
Vite passes back: browser gets the response
```

The browser thinks it's talking to port 3000 the whole time — no CORS issues.

---

### Step 7: CSS Styling

Created a clean, professional design system in `index.css`:
- **CSS Variables** — consistent colors, spacing, shadows used everywhere
- **Dashboard cards** — CSS Grid with `repeat(auto-fill, minmax(200px, 1fr))` for responsive layout
- **Data tables** — styled with hover effects, uppercase headers, bordered rows
- **Status badges** — color-coded: PENDING (yellow), COMPLETED (green), CANCELLED (red), IN_PROGRESS (blue)
- **Responsive** — `@media (max-width: 768px)` breakpoints for mobile

---

### Key TypeScript Lesson: `verbatimModuleSyntax`

**What happened:** Build failed with "Missing export" errors for TypeScript interfaces.

**Why:** The tsconfig has `verbatimModuleSyntax: true`, which means TypeScript enforces that type-only imports use the `import type` syntax. The bundler (rolldown) strips type exports at build time, so if you import them as values, they don't exist.

**Fix:**
```typescript
// WRONG — imports types as values:
import { authApi, LoginRequest, RegisterRequest, LoginResponse } from '../api/auth';

// CORRECT — separates type imports:
import { authApi } from '../api/auth';
import type { LoginRequest, RegisterRequest, LoginResponse } from '../api/auth';
```

**Rule:** If you're only using something as a type annotation (not calling it at runtime), use `import type`.

---

### Step 8: Google Sign-In & Social Login Integration

**What was added:** Integrated Google's "Sign in with Google" button on both Login and Register pages, connecting the frontend to the backend's existing social auth (Google, Facebook, GitHub) and JWT token system.

**The complete authentication flow — how 5 systems work together:**
```
                 Frontend                    Backend
                 ────────                    ───────
                 ┌─────────────┐
Step 1:          │ Login Page  │
User picks a     │             │
login method     │ ┌─────────┐ │
                 │ │Username │ │    ─── REGULAR LOGIN ───
                 │ │Password │──────→ POST /api/auth/login
                 │ │ [Login] │ │     AuthService.login()
                 │ └─────────┘ │       → AuthenticationManager checks password
                 │             │       → JwtTokenProvider.generateToken(username, role)
                 │ ───── or ────│       → returns { token, username, role }
                 │             │
                 │ ┌─────────┐ │    ─── GOOGLE LOGIN ───
Step 2:          │ │ Sign in │ │
Google popup     │ │  with   │ │    1. Google popup → user logs in
opens            │ │ Google  │ │    2. Google returns ID token (credential)
                 │ └────┬────┘ │    3. Frontend sends to POST /api/auth/google
                 │      │      │       → GoogleIdTokenVerifier.verify(token)
                 │      │      │       → extracts email, name from Google payload
                 │      │      │       → finds existing user OR creates new account
                 │      │      │       → JwtTokenProvider.generateToken(username, role)
                 │      │      │       → returns SAME { token, username, role }
                 └──────┼──────┘
                        │
Step 3:          ┌──────┼──────┐
Frontend saves   │ AuthContext  │    Both login paths return the SAME LoginResponse:
JWT + role       │              │      { token: "eyJ...", username: "john", role: "ROLE_PATIENT" }
                 │ localStorage │    Frontend doesn't care HOW you logged in —
                 │   token ✓    │    it just stores the JWT and uses it for all API calls.
                 │   user  ✓    │
                 └──────┼──────┘
                        │
Step 4:          ┌──────┼──────┐
Role-based       │ redirectByRole│    ROLE_PATIENT → /patient/dashboard
redirect         │              │    ROLE_DOCTOR  → /doctor/dashboard
                 │ ProtectedRoute│   ROLE_ADMIN   → /admin/dashboard
                 └──────┼──────┘
                        │
Step 5:          ┌──────┼──────┐    ┌─────────────────────┐
API calls use    │ Axios inter- │    │ JwtAuthFilter       │
JWT for every    │ ceptor adds  │───→│ reads Bearer token  │
request          │ Bearer header│    │ validates signature │
                 └─────────────┘    │ sets SecurityContext│
                                    │ SecurityConfig      │
                                    │ checks role match   │
                                    └─────────────────────┘
```

**Files modified:**

| File | What Changed |
|------|-------------|
| `index.html` | Added Google Identity Services script (`accounts.google.com/gsi/client`) |
| `.env` | Added `VITE_GOOGLE_CLIENT_ID` environment variable |
| `api/auth.ts` | Added `GoogleLoginRequest`, `SocialLoginRequest` interfaces + `googleLogin()`, `socialLogin()` methods |
| `context/AuthContext.tsx` | Added `googleLogin()` and `socialLogin()` to context + `saveUserData()` helper |
| `pages/auth/LoginPage.tsx` | Added Google Sign-In button, `handleGoogleCallback`, `redirectByRole` |
| `pages/auth/RegisterPage.tsx` | Added Google Sign-Up button, portal type applies to social signup too |
| `index.css` | Added `.social-login-section`, `.google-btn-container`, `.auth-divider` styles |

**Key concepts learned:**

**1. Google Identity Services (GIS):**
```html
<!-- Load Google's library in index.html -->
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

```typescript
// Initialize in React component
window.google.accounts.id.initialize({
  client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
  callback: handleGoogleCallback,  // Called when user completes Google login
});

// Render the official Google button
window.google.accounts.id.renderButton(element, {
  theme: 'outline', size: 'large', text: 'signin_with'
});
```

**2. The callback flow:**
```typescript
// Google calls this function after the user signs in
const handleGoogleCallback = async (response: { credential: string }) => {
  // response.credential = a signed JWT from Google containing:
  //   email, name, picture, email_verified, etc.
  // We DON'T decode it — we send it to our backend for verification

  await googleLogin({
    googleToken: response.credential,  // Google's token
    portalType: 'patient',              // What role to assign if new user
  });

  // Backend returns OUR JWT — from here it's identical to regular login
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  redirectByRole(user.role);
};
```

**3. Why both tokens exist:**
```
Google's ID token          Our JWT token
─────────────────          ─────────────
Proves: "This person       Proves: "This person
  is john@gmail.com"         is ROLE_PATIENT in
                              our system"

Used: ONCE during           Used: EVERY API call
  login (to verify           (Bearer header)
  identity)

Expires: ~1 hour            Expires: 24 hours
  (doesn't matter —          (set in our
  we only use it once)        application.properties)

Issued by: Google            Issued by: our
                              JwtTokenProvider
```

**4. Role-based access — enforced at TWO layers:**
```
LAYER 1: Frontend (ProtectedRoute.tsx)
  — Prevents UI navigation to wrong portal
  — if user.role !== requiredRole → redirect

LAYER 2: Backend (SecurityConfig.java)
  — Prevents API access even if frontend is bypassed
  — /api/patient/** → hasRole('PATIENT')
  — /api/doctor/**  → hasRole('DOCTOR')
  — /api/admin/**   → hasRole('ADMIN')

BOTH layers check the same role from the same JWT token.
Even if someone manually types /admin/dashboard in the URL,
  the frontend redirects them away (Layer 1),
  AND the backend rejects their API calls with 403 (Layer 2).
```

**5. Vite environment variables:**
```bash
# .env file — only VITE_ prefixed vars are exposed to frontend
VITE_GOOGLE_CLIENT_ID=123456789.apps.googleusercontent.com

# In code — accessed via import.meta.env
const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

# Why the prefix? Security — prevents accidentally exposing
# server-side secrets (DB passwords, API keys) to the browser
```

**6. `declare global` for third-party scripts:**
```typescript
// Google's library adds window.google, but TypeScript doesn't know about it
// We declare the type ourselves so TypeScript can check our usage
declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {...}) => void;
          renderButton: (element: HTMLElement, config: {...}) => void;
        };
      };
    };
  }
}
```

---

### Frontend Build Result

```
npx vite build
✓ 109 modules transformed
✓ built in 203ms

dist/index.html          0.75 kB
dist/assets/index.css    7.22 kB (gzip: 1.93 kB)
dist/assets/index.js   348.10 kB (gzip: 97.64 kB)
```

---

### Step 9: Full Endpoint Coverage (Gap Fill)

**Goal:** Make the frontend cover 100% of the 73 backend API endpoints.

**What was missing and what was added:**

**Patient portal gaps filled (3 endpoints):**
- `PatientDoctor.tsx` — NEW page showing assigned primary doctor info (specialization, hospital, fee)
- `PatientPrescriptions.tsx` — UPDATED with "Active Only" filter button (calls `GET /prescriptions/active`)
- `PatientLabOrders.tsx` — UPDATED with status filter buttons (ALL/ORDERED/IN_PROGRESS/COMPLETED/CANCELLED)

**Doctor portal gaps filled (3 endpoints):**
- `DoctorProfile.tsx` — NEW page for viewing/editing doctor profile (specialization, phone, hospital, experience, fee)
- `DoctorVisits.tsx` — UPDATED with inline editing (Edit button → edit diagnosis/notes in-place → Save)
- `doctor.ts` API — added `updateVisit()` method

**Admin portal gaps filled (32 endpoints):**
- `AdminUsers.tsx` — UPDATED: added "Role" button to change user roles (prompts for new role)
- `AdminPatients.tsx` — REBUILT with full CRUD: search by name, create, edit (inline form), assign doctor, delete
- `AdminDoctors.tsx` — REBUILT with full CRUD: search by name, create, edit, delete
- `AdminAppointments.tsx` — UPDATED: added Cancel + Delete buttons
- `AdminVisits.tsx` — NEW page: view all visits, date range filter, delete
- `AdminPrescriptions.tsx` — NEW page: view all prescriptions, delete
- `AdminLabOrders.tsx` — NEW page: view all lab orders, status filter, delete
- `AdminPharmacies.tsx` — NEW page: full CRUD (create/edit/delete/search)
- `admin.ts` API — REBUILT with ALL 41 endpoints covered

**Other changes:**
- `App.tsx` — added 6 new routes (patient/my-doctor, doctor/profile, admin/visits, admin/prescriptions, admin/lab-orders, admin/pharmacies)
- `Navbar.tsx` — added "My Doctor" and "Profile" links to patient/doctor navs; added Visits, Prescriptions, Lab Orders, Pharmacies to admin nav
- `index.css` — added `.search-bar`, `.filter-group`, `.inline-input` styles

**Final coverage:**
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

## Phase 4: PostgreSQL Database Connection

### Why Switch from H2 to PostgreSQL?

**H2 (what we had before):**
- In-memory database — lives in RAM only
- All data disappears every time you restart the app
- Great for learning and quick testing, but not for real use

**PostgreSQL (what we switched to):**
- Real database — data stored on disk
- Data **persists** across restarts — users, patients, appointments are all saved permanently
- Same database used by companies like Instagram, Spotify, and Netflix
- Already installed on this machine (via Postgres.app)

### What Changed

**1. pom.xml — Added PostgreSQL driver, moved H2 to test-only:**
```xml
<!-- BEFORE: H2 as the runtime database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>      <!-- available when app runs -->
</dependency>

<!-- AFTER: PostgreSQL for runtime, H2 only for tests -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>      <!-- JDBC driver for PostgreSQL -->
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>         <!-- only available during tests now -->
</dependency>
```

**Why `scope` matters:**
- `runtime` = needed to run the app, but your Java code never imports it directly
- `test` = only available when running `./mvnw test`, not in the final app
- Spring Boot auto-detects the database driver and configures the connection

**2. application.properties — Database connection settings:**
```properties
# BEFORE: H2 in-memory
spring.datasource.url=jdbc:h2:mem:labservicedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop

# AFTER: PostgreSQL persistent
spring.datasource.url=jdbc:postgresql://localhost:5432/labservicedb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=meeraramesh
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

**Key differences explained:**
| Setting | H2 Value | PostgreSQL Value | Why |
|---------|----------|-----------------|-----|
| `url` | `jdbc:h2:mem:labservicedb` | `jdbc:postgresql://localhost:5432/labservicedb` | H2 runs in memory, PostgreSQL runs as a separate service on port 5432 |
| `driver` | `org.h2.Driver` | `org.postgresql.Driver` | Each database has its own JDBC driver class |
| `username` | `sa` | `meeraramesh` | H2 uses default "sa", PostgreSQL uses your system user |
| `ddl-auto` | `create-drop` | `update` | `create-drop` destroys tables on shutdown; `update` preserves data and only adds missing tables/columns |

**3. New properties needed for PostgreSQL:**
```properties
# Run data.sql even with a real database
spring.sql.init.mode=always
# ↑ Default is "embedded" which only runs data.sql for H2/HSQLDB
# ↑ "always" tells Spring to run it for any database

# Defer data.sql until AFTER Hibernate creates tables
spring.jpa.defer-datasource-initialization=true
# ↑ Without this, data.sql runs BEFORE tables exist → crash!
# ↑ This tells Spring: "wait for Hibernate to create tables first"
```

**4. data.sql — PostgreSQL-safe insert:**
```sql
-- BEFORE: Simple insert (crashes on duplicate)
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2b$10$...', 'admin@labservice.com', 'ROLE_ADMIN', true);

-- AFTER: Idempotent insert (safe to run on every restart)
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2b$10$...', 'admin@labservice.com', 'ROLE_ADMIN', true)
ON CONFLICT (username) DO NOTHING;
```

**Why `ON CONFLICT DO NOTHING`?**
- With H2 + `create-drop`, the users table was destroyed and recreated every restart, so the INSERT always worked
- With PostgreSQL + `update`, the table survives restarts, so the admin user already exists on the second run
- Without `ON CONFLICT`, the INSERT would fail with a "duplicate key" error
- `ON CONFLICT (username) DO NOTHING` = "if a user with this username already exists, skip this insert"

### Key Concepts Learned

**JDBC (Java Database Connectivity):**
- A standard Java API for connecting to ANY database
- The URL format tells Java which database to connect to:
  - `jdbc:h2:mem:dbname` → H2 in memory
  - `jdbc:postgresql://host:port/dbname` → PostgreSQL
  - `jdbc:mysql://host:port/dbname` → MySQL
- You never write JDBC code directly — Spring Data JPA does it for you

**ddl-auto modes:**
```
create-drop  → Create tables on start, DROP everything on stop (data lost!)
create       → Create tables on start (overwrites existing)
update       → Add missing tables/columns, keep existing data (what we use)
validate     → Don't change anything, just check tables match entities
none         → Do nothing (you manage the schema manually)
```

**How to query the database directly:**
```bash
# Connect to PostgreSQL:
psql -h localhost -U meeraramesh -d labservicedb

# Useful commands inside psql:
\dt              -- list all tables
\d users         -- describe the users table (columns, types)
SELECT * FROM users;    -- see all users
SELECT * FROM patients; -- see all patients
\q               -- quit
```

### Verification

```bash
# 1. PostgreSQL is running:
pg_isready
# → /tmp:5432 - accepting connections

# 2. Database was created:
psql -h localhost -U meeraramesh -d labservicedb -c "SELECT 1"
# → 1 row returned

# 3. Backend starts and connects:
./mvnw spring-boot:run
# → "Started LabServiceApplication in X.X seconds"
# → HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection

# 4. Admin login works:
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# → {"token":"eyJ...","username":"admin","role":"ROLE_ADMIN"}

# 5. Data persists across restarts:
psql -h localhost -U meeraramesh -d labservicedb \
  -c "SELECT id, username, role FROM users;"
# → admin | ROLE_ADMIN (still there after restart!)
```
