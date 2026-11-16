# Sword Android Architecture

## Architectural Pattern
Sword Android follows **Clean Architecture + MVVM** with unidirectional reactive state flow (`StateFlow`):

- **UI Layer (`ui/`)**: 13 Jetpack Compose Material 3 screens (`Splash`, `Onboarding`, `Dashboard`, `ModulesExplorer`, `ModuleDetail`, `ScanConfig`, `LiveExecution`, `ScanHistory`, `ReportsCenter`, `DeviceCapabilities`, `PermissionsManager`, `Settings`, `About`) driven by `SwordViewModel`.
- **Core Infrastructure (`core/`)**:
  - `ExecutionEngine`: Asynchronous coroutine engine with `Semaphore(2)` concurrency limits, per-task `Job` tracking & cancellation, `withTimeout` bounds, `measureTimeMillis` benchmarking, structured error classification (`ExecutionErrorType`), and automatic Room + Report file persistence.
  - `CapabilityManager` & `PermissionManager`: Evaluates Android API level, `ConnectivityManager`, `WifiManager`, `LocationManager`, runtime permissions, optional `su` presence (never escalated), and Chaquopy Python 3.10 readiness to assign each module a live `CapabilityState` (`AVAILABLE`, `AVAILABLE_WITH_LIMITATIONS`, `REQUIRES_PERMISSION`, `UNSUPPORTED`, `NOT_IMPLEMENTED`).
  - `InputValidator`: Strict typed validation before any network or diagnostic operation.
  - `PythonBridge`: Thread-safe `Chaquopy` Kotlin-to-Python bridge running exclusively on `Dispatchers.IO`.
- **Data Layer (`data/`)**:
  - `SwordDatabase` (Room v1): `scan_records`, `report_metadata`, `evidence_records`, `activity_logs`.
  - `UserPreferencesRepository` (DataStore): Onboarding flag, animation toggle, authorization confirmation gate, timeouts, concurrency limit, DoH provider, proxy URL, and language preference.
  - `ReportStorageManager`: Writes canonical-path-validated JSON & styled HTML reports in `filesDir/reports/` and exports via `FileProvider` or Android SAF.
  - `ModuleRegistry`: Authoritative metadata & parameter definitions for all Sword Android tools.
- **Domain Executors (`domain/tools/`)**: Independent, testable Kotlin executors for Network, Web Security, OSINT, Wireless Info, Cryptography, Forensics, and Custom Presets.

## Embedded Python Evaluation (Chaquopy 16.0.0)
- **Why Chaquopy**: Packages Python 3.10 directly inside the APK (`arm64-v8a`, `armeabi-v7a`, `x86_64`) with zero external Termux or Python dependency.
- **Selective Reuse**: Native Kotlin handles all Android system APIs, networking (`OkHttp`), TLS (`SSLSocket`), forensics (`ExifInterface`), and UI. Python is selectively used for `sword_py.username_engine` (reusing Sword's `wmn-data.json` + wildcard false-positive logic) and pure-Python `sword_py.validators` / `sword_py.crypto_utils`.
- **Android Wheel Compatibility**: Replaced `aiohttp` (which requires native compilation/wheels) and `rich` (terminal UI) with pure-Python stdlib (`urllib.request`, `ssl`, `concurrent.futures`, `json`, `hashlib`), avoiding bloat and native build failures.
