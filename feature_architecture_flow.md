# Feature Architecture Flow - Payroll Management System (Comprehensive)

This document provides a deep dive into the architectural flow of **every feature** implemented in the project, from the smallest UI detail to the largest backend integration.

---

## 1. Global Core Architecture
Before diving into individual features, remember that every feature follows this **Unidirectional Data Flow**:
1.  **User Action** (UI Layer)
2.  **Request Trigger** (ViewModel Layer via Coroutines)
3.  **Data Retrieval** (Repository Layer via Retrofit)
4.  **Security Check** (OkHttp Interceptor Layer)
5.  **State Update** (LiveData updates in ViewModel)
6.  **UI Refresh** (View observes changes and re-renders)

---

## 2. Authentication & Session Features

### A. Splash Screen (Initial Handshake)
- **Purpose**: Brand visibility and session pre-check.
- **Flow**: `SplashActivity` -> Timer (2 seconds) -> Check `SessionManager.isLoggedIn()` -> Navigate to `MainActivity` (if false) or `SecondActivity` (if true).

### B. Dual-Login (Big Feature)
- **Purpose**: Authenticating with two independent AWS clusters (EMS and PMS).
- **Flow**:
    - **UI**: Email/Password entered in `MainActivity`.
    - **VM**: `LoginViewModel.dualLogin()` is triggered.
    - **Repo**: `DualLoginRepository` executes TWO parallel POST requests.
    - **Logic**: If either succeeds, the app saves the available token. If both succeed, the app is "Fully Authenticated".
    - **Storage**: `SessionManager` persists the JWT tokens and Employee IDs to SharedPreferences.

### C. Automatic Header Injection (Security Feature)
- **Flow**: Any Repository -> `RetrofitClient` -> **OkHttp Interceptor** -> `SessionManager.fetchToken()`.
- **Logic**: The Interceptor detects if the URL is for PMS or EMS and injects the corresponding token into the `Authorization: Bearer` header automatically.

---

## 3. Attendance & Calendar Features

### A. Monthly History Fetching (Big Feature)
- **Flow**: `Calender.kt` -> `CalendarViewModel` -> `CombinedRepository` -> `PMS API (/history)`.
- **Logic**: Returns a list of `AttendanceResponse` objects containing timestamps.

### B. Calendar Dot Indicators (Small UI Feature)
- **Flow**: `CalendarMonthAdapter` receives the attendance list.
- **Logic**: It iterates through the days. If a day matches a record in the list, it draws a **Green Dot** (Present). If no record is found and it's not a weekend, it draws a **Red Dot** (Absent).

### C. Weekend Highlighting (Small UI Feature)
- **Flow**: `CalendarMonthAdapter` checks `Calendar.DAY_OF_WEEK`.
- **Logic**: If the day is Saturday or Sunday, it applies the `calendar_day_weekend_bg.xml` drawable to the cell background.

---

## 4. Leave Management Features

### A. Leave Balance Display (Big Feature)
- **Flow**: `LeaveRequestActivity` -> `LeaveViewModel` -> `LeaveRepository` -> `EMS API (/balance)`.
- **Logic**: Fetches `totalLeaves`, `consumedLeaves`, and `remainingLeaves`.

### B. Leave Application Form (Big Feature)
- **Flow**: `ApplyLeaveActivity` -> User selects date and type -> `LeaveViewModel.applyLeave()`.
- **Validation**: VM checks if the reason is not empty and dates are valid before hitting the `EMS API (/apply)`.

### C. Status Badge Display (Small UI Feature)
- **Flow**: `LeaveHistoryAdapter` binds leave records.
- **Logic**: It checks the `status` string (Pending/Approved/Rejected) and dynamically changes the color of the status badge.

---

## 5. Payroll & Payslip Features

### A. Salary Details Fetching (Big Feature)
- **Flow**: `PayslipActivity` -> `PayrollViewModel` -> `PayrollRepository` -> `PMS API (/salary)`.
- **Logic**: Fetches a breakdown of Earnings (Basic, HRA) and Deductions (PF, Tax).

### B. Month/Year Filtering (Small Feature)
- **Flow**: `FilterPayslipActivity` -> DatePicker selection -> Intent Extras -> `PayslipActivity`.
- **Logic**: The selected month/year are passed as query parameters to the API request.

### C. PDF Download & Sharing (Big Feature)
- **Flow**: `PayrollViewModel.downloadPayslipByMonth()` -> `PMS API (/pdf)`.
- **Logic**: Returns a `ResponseBody` (byte stream).
- **Storage**: App saves the stream as a `.pdf` file in the device's Cache directory.
- **Provider**: Uses `FileProvider` to create a secure URI, allowing the user to view or share the PDF via external apps.

---

## 6. Dashboard & Utility Features

### A. Dashboard Quick Stats (Big Feature)
- **Flow**: `SecondActivity` -> `DashboardViewModel` -> `CombinedRepository.fetchDashboardData()`.
- **Logic**: Simultaneously fetches Attendance, Leave Balance, and Holidays to show a "Summary View" on the dashboard.

### B. Holiday List (Big Feature)
- **Flow**: `HolidayActivity` -> `HolidayViewModel` -> `HolidayRepository` -> `PMS API (/holidays)`.
- **Logic**: Displays upcoming company holidays in a sorted list via `HolidayAdapter`.

### C. Debug Inspection (Utility Feature)
- **Flow**: `DebugDataActivity` -> Button Click -> Direct `RetrofitClient` call.
- **Logic**: Bypasses the ViewModel layer to show "Raw JSON" on the screen. Used for verifying backend data during development.

---

## 7. Error Handling & Stability Features

### A. API Fallback (Dummy Data)
- **Flow**: `Repository` returns `Result.failure`.
- **Logic**: `ViewModel` catches the error, logs it, and calls `getDummyData()` to ensure the UI doesn't look broken to the user.

### B. ViewBinding Stability (Crash Prevention)
- **Flow**: `Activity` creates `_binding` reference.
- **Logic**: In `onDestroy`, the app sets `_binding = null` to prevent memory leaks and "IllegalStateExceptions" when the UI is no longer visible.

### C. Loading States (UX Feature)
- **Flow**: `ViewModel` sets `_isLoading.value = true` before the API call and `false` after.
- **Logic**: The Activity observes `isLoading` and shows/hides a `ProgressBar` overlay.
