# React 19 Features Used in LabService Frontend

This document details every React 19 feature incorporated into the LabService frontend, explaining what each feature does, why it was chosen, and where it is used.

---

## 1. `useActionState`

### What It Does

`useActionState` is a React 19 hook that manages form submission state in a single call. It replaces the common pattern of combining multiple `useState` hooks for error messages, success messages, and loading indicators.

```js
const [state, submitAction, isPending] = useActionState(asyncFunction, initialState);
```

- **`state`** — The current return value of the action (e.g., `{ error, success }`)
- **`submitAction`** — A function passed directly to `<form action={...}>`
- **`isPending`** — A boolean that is `true` while the async action is in flight

### Why It Was Chosen

Before React 19, every form required boilerplate like this:

```js
const [error, setError] = useState('');
const [success, setSuccess] = useState('');
const [loading, setLoading] = useState(false);

const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setSuccess('');
  setLoading(true);
  try {
    // ...api call
    setSuccess('Done!');
  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
};
```

With `useActionState`, all of this collapses into a single hook. There is no need to call `e.preventDefault()`, no manual loading state management, and the error/success state is derived from the action's return value.

### Where It Is Used

| File | Purpose |
|------|---------|
| `pages/auth/LoginPage.js` | Login form submission |
| `pages/auth/RegisterPage.js` | Registration form submission |
| `pages/auth/ForgotPasswordPage.js` | Forgot password email request |
| `pages/auth/ResetPasswordPage.js` | Password reset with token |
| `pages/patient/PatientAppointments.js` | Booking a new appointment |
| `pages/patient/PatientProfile.js` | Updating patient profile |
| `pages/doctor/DoctorVisits.js` | Recording a new visit |
| `pages/doctor/DoctorPrescriptions.js` | Writing a new prescription |
| `pages/doctor/DoctorLabOrders.js` | Ordering a new lab test |
| `pages/doctor/DoctorProfile.js` | Updating doctor profile |

### Example (from `DoctorPrescriptions.js`)

```js
const [state, submitAction, isPending] = useActionState(async (_prev, formData) => {
  try {
    await doctorApi.writePrescription({
      patientId: Number(formData.get('patientId')),
      medicationName: formData.get('medicationName'),
      dosage: formData.get('dosage'),
      frequency: formData.get('frequency'),
      startDate: formData.get('startDate'),
      endDate: formData.get('endDate') || undefined,
      notes: formData.get('notes'),
    });
    setShowForm(false);
    loadPrescriptions();
    return { error: null, success: 'Prescription created!' };
  } catch (err) {
    return { error: err.response?.data?.message || 'Failed to create prescription.', success: null };
  }
}, { error: null, success: null });
```

---

## 2. `<form action={...}>` (Form Actions)

### What It Does

React 19 allows passing a function directly to a form's `action` attribute. When the form is submitted, React automatically:

1. Prevents the default browser submission
2. Collects all named inputs into a `FormData` object
3. Passes that `FormData` to the action function

```jsx
<form action={submitAction}>
  <input type="text" name="username" required />
  <button type="submit">Submit</button>
</form>
```

### Why It Was Chosen

This eliminates the need for `onSubmit` handlers that call `e.preventDefault()`. It also pairs naturally with `useActionState`, which returns a `submitAction` function designed for this purpose. The form inputs no longer need to be controlled components — instead, values are read from the native `FormData` API via `formData.get('fieldName')`.

### Where It Is Used

Every form that uses `useActionState` also uses `<form action={...}>`:

- All 4 auth pages (login, register, forgot password, reset password)
- Patient: appointment booking, profile update
- Doctor: visit recording, prescription creation, lab ordering, profile update

### Example (from `PatientProfile.js`)

```jsx
<form className="profile-form" action={saveAction}>
  <input type="text" name="phoneNumber" defaultValue={profile.phoneNumber || ''} />
  <input type="text" name="address" defaultValue={profile.address || ''} />
  <input type="date" name="dateOfBirth" defaultValue={profile.dateOfBirth || ''} />
  <select name="gender" defaultValue={profile.gender || ''}>
    <option value="">Select</option>
    <option value="MALE">Male</option>
    <option value="FEMALE">Female</option>
    <option value="OTHER">Other</option>
  </select>
  <button type="submit" disabled={isSaving}>
    {isSaving ? 'Saving...' : 'Save'}
  </button>
</form>
```

Note the use of `defaultValue` instead of `value` + `onChange`. Since the form action reads from `FormData`, there is no need to keep React state in sync with every keystroke.

---

## 3. `useOptimistic`

### What It Does

`useOptimistic` provides a way to show an optimistic (predicted) UI state while an async operation is still in progress. If the operation fails, the optimistic state is automatically rolled back when the real state updates.

```js
const [optimisticItems, addOptimistic] = useOptimistic(
  items,                          // the real source of truth
  (currentItems, optimisticValue) => {
    // return a new array reflecting the predicted outcome
  }
);
```

- **`optimisticItems`** — The list to render (includes optimistic changes)
- **`addOptimistic`** — Call this to apply an optimistic update before the server responds

### Why It Was Chosen

In a healthcare application, users expect immediate feedback when they click "Cancel" or "Delete". Without optimistic updates, the UI would freeze or show a spinner until the server responds. With `useOptimistic`, the item visually disappears (or changes status) the instant the user clicks, and the real data is reconciled after the API call completes.

### Where It Is Used

| File | Optimistic Behavior |
|------|-------------------|
| `pages/patient/PatientAppointments.js` | Cancelled appointment immediately shows "CANCELLED" status |
| `pages/doctor/DoctorLabOrders.js` | Status changes (Start, Complete) reflect instantly |
| `pages/admin/AdminUsers.js` | Enable/disable toggles and deletes update instantly |
| `pages/admin/AdminPatients.js` | Deleted patient disappears from list instantly |
| `pages/admin/AdminDoctors.js` | Deleted doctor disappears from list instantly |
| `pages/admin/AdminVisits.js` | Deleted visit disappears from list instantly |
| `pages/admin/AdminPrescriptions.js` | Deleted prescription disappears from list instantly |
| `pages/admin/AdminLabOrders.js` | Deleted lab order disappears from list instantly |
| `pages/admin/AdminAppointments.js` | Cancelled appointment shows new status instantly; deleted appointment disappears instantly |
| `pages/admin/AdminPharmacies.js` | Deleted pharmacy disappears from list instantly |

### Example (from `AdminAppointments.js`)

```js
const [optimisticAppointments, updateOptimistic] = useOptimistic(
  appointments,
  (current, action) => {
    if (action.type === 'cancel') {
      return current.map(a => a.id === action.id ? { ...a, status: 'CANCELLED' } : a);
    }
    if (action.type === 'delete') {
      return current.filter(a => a.id !== action.id);
    }
    return current;
  }
);

const handleCancel = async (id) => {
  if (!window.confirm('Cancel this appointment?')) return;
  updateOptimistic({ type: 'cancel', id });   // instant UI update
  try {
    await adminApi.cancelAppointment(id);       // server call
    loadAppointments();                          // reconcile with real data
  } catch (err) {
    loadAppointments();                          // rollback on failure
  }
};
```

---

## 4. `useTransition`

### What It Does

`useTransition` marks a state update as non-urgent (a "transition"), allowing React to keep the UI responsive while processing the update in the background. It returns a pending flag and a `startTransition` function.

```js
const [isPending, startTransition] = useTransition();
```

- **`isPending`** — `true` while the transition is in progress
- **`startTransition`** — Wraps a state update to mark it as low-priority

In React 19, `startTransition` supports async functions, meaning you can await API calls inside a transition.

### Why It Was Chosen

Filter and search operations fetch new data from the server. Without `useTransition`, clicking a filter button would block the UI — the user could not interact with anything until the API call finished. With `useTransition`, the old data stays visible and interactive while the new data loads in the background. The `isPending` flag is used to show a subtle "Filtering..." indicator and disable filter buttons to prevent duplicate requests.

### Where It Is Used

| File | Transition Wraps |
|------|-----------------|
| `pages/patient/PatientPrescriptions.js` | Switching between "All" and "Active Only" filters |
| `pages/patient/PatientLabOrders.js` | Switching between status filters (ALL, ORDERED, IN_PROGRESS, COMPLETED, CANCELLED) |
| `pages/admin/AdminPatients.js` | Searching patients by last name |
| `pages/admin/AdminDoctors.js` | Searching doctors by name |
| `pages/admin/AdminPharmacies.js` | Searching pharmacies by name |
| `pages/admin/AdminVisits.js` | Filtering visits by date range |
| `pages/admin/AdminLabOrders.js` | Switching between status filters |

### Example (from `PatientLabOrders.js`)

```js
const [isFiltering, startFilterTransition] = useTransition();

const handleStatusChange = (status) => {
  setStatusFilter(status);                    // immediate: highlight the clicked button
  startFilterTransition(async () => {         // non-blocking: fetch in background
    try {
      await loadLabOrders(status);
    } catch (err) {
      console.error(err);
    }
  });
};
```

In the JSX, the pending state is used to disable buttons and show feedback:

```jsx
<button
  className={`btn btn-sm ${statusFilter === s ? 'btn-primary' : 'btn-secondary'}`}
  onClick={() => handleStatusChange(s)}
  disabled={isFiltering}
>
  {s === 'ALL' ? 'All' : s.replace('_', ' ')}
</button>
{isFiltering && <div className="loading">Filtering...</div>}
```

---

## 5. `FormData` API (with `formData.get()`)

### What It Does

When a form uses `<form action={...}>`, React passes a native `FormData` object to the action function. Each input's value is retrieved by its `name` attribute:

```js
const username = formData.get('username');
const email = formData.get('email');
```

### Why It Was Chosen

This replaces the pattern of managing every input with `useState` (controlled components). For forms where you only need the values at submission time (not during typing), uncontrolled inputs with `defaultValue` and `FormData` are simpler and produce less code.

### Where It Is Used

Every form that uses `<form action={...}>` reads values via `FormData`:

- Auth forms: `formData.get('username')`, `formData.get('password')`, `formData.get('email')`, etc.
- Patient profile: `formData.get('phoneNumber')`, `formData.get('address')`, `formData.get('dateOfBirth')`, etc.
- Doctor forms: `formData.get('patientId')`, `formData.get('testName')`, `formData.get('medicationName')`, etc.

### Example (from `LoginPage.js`)

```js
const [error, submitAction, isPending] = useActionState(async (_prevState, formData) => {
  try {
    await login({
      username: formData.get('username'),
      password: formData.get('password'),
    });
    // ...
  } catch (err) {
    return err.response?.data?.message || 'Login failed.';
  }
}, null);
```

---

## Summary Table

| React 19 Feature | Replaces | Benefit |
|---|---|---|
| `useActionState` | Multiple `useState` hooks + manual `try/catch/finally` | Single hook manages error, success, and pending state |
| `<form action={...}>` | `onSubmit` + `e.preventDefault()` | Declarative form handling, no manual event wiring |
| `FormData` + `defaultValue` | Controlled inputs (`value` + `onChange` + `useState`) | Less boilerplate for submit-only forms |
| `useOptimistic` | Manual optimistic state or waiting for server response | Instant UI feedback with automatic rollback on failure |
| `useTransition` | `setLoading(true)` before API calls | Non-blocking filters/searches that keep the UI responsive |

---

## Pages That Were Not Changed

The following pages are read-only (no forms, no filters, no delete actions) and did not benefit from React 19 hooks:

- `pages/patient/PatientDashboard.js` — displays stat cards and tables
- `pages/patient/PatientVisits.js` — read-only visit history table
- `pages/patient/PatientDoctor.js` — read-only doctor info display
- `pages/doctor/DoctorDashboard.js` — displays stat cards and tables
- `pages/doctor/DoctorPatients.js` — read-only patient list
- `pages/doctor/DoctorAppointments.js` — read-only appointment list
- `pages/admin/AdminDashboard.js` — displays 10 stat cards
- `components/Navbar.js` — navigation links based on role
- `components/ProtectedRoute.js` — route guard (auth + role check)
