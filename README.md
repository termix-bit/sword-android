# Sword Android — Android Security Toolkit

**Sword Android** is an independent, native Android cybersecurity diagnostic, OSINT, cryptography, and digital forensics toolkit built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, **DataStore**, **OkHttp**, and an optional **Chaquopy Embedded Python 3.10** runtime.

> **Product Family**
> - **Sword Termux** — Terminal Security Framework
> - **Sword Linux** — Desktop/Server Security Suite
> - **Sword Android** — Native Android Security Toolkit *(this repository)*

Sword Android is **not** a WebView wrapper or a Termux frontend. It is engineered from the ground up around Android's security sandbox, Scoped Storage (SAF), `ConnectivityManager` / `WifiManager`, `ExifInterface`, and encrypted DNS-over-HTTPS (DoH).

---

## Core Capabilities

- **Network**: Interface & `LinkProperties` inspection, RFC 8484 DNS-over-HTTPS lookup (`A`, `AAAA`, `MX`, `TXT`, `NS`, `CNAME`, `SOA`), connectivity & TCP RTT latency benchmark, bounded authorized TCP port probe (up to 64 ports), and authorized local `/24` LAN discovery.
- **Web Security**: HTTP security header analyzer (`HSTS`, `CSP`, `X-Frame-Options`, `X-Content-Type-Options`, cookie flags), X.509 TLS certificate & cipher inspector, and passive website configuration audit (`/.well-known/security.txt`, `/robots.txt`).
- **OSINT**: HTTPS RDAP/WHOIS domain lookup, HTTPS IP & ASN intelligence, SPF/DMARC/MX email DNS posture audit, and rate-limited username availability checks powered by bundled Python 3.10 and the WhatsMyName dataset (`CC BY-SA 4.0`).
- **Wireless Information**: Active Wi-Fi connection diagnostics and nearby AP security scan (`WifiManager`) with full Android Location/Nearby Wi-Fi permission transparency and throttling awareness.
- **Cryptography**: SHA-256 / SHA-512 digest generation (legacy MD5/SHA-1 flagged), constant-time hash verification, Base64/Hex/URL/ROT13 utilities, and `SecureRandom` token generator.
- **Forensics**: Android SAF file metadata & magic-byte signature detector, streaming chain-of-custody SHA-256/512 hashing persisted to Room, `ExifInterface` GPS/device privacy audit, and memory-bounded hex dump + printable strings extractor.
- **Reports & Persistence**: Room database for scan history, report metadata, forensics evidence records, and activity logs; app-private JSON & Sword-branded HTML report generation, SAF export, FileProvider sharing, and one-tap deletion.
- **Device Capabilities & Permissions Manager**: Live detection of Android API level, transport/VPN state, Location Services, runtime permissions, root binary presence (optional, non-required), and Chaquopy Python 3.10 health.

---

## Project Structure

```text
Sword-Android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── python/sword_py/          # Bundled Python 3.10 modules + wmn-data.json
│       │   ├── res/                      # Sword emblem, adaptive icons, dark navy/cyan theme
│       │   └── java/com/sword/security/android/
│       │       ├── core/                 # CapabilityManager, PermissionManager, ExecutionEngine, InputValidator, PythonBridge
│       │       ├── data/                 # Room DB, DataStore Prefs, ReportStorageManager, ModuleRegistry
│       │       ├── domain/tools/         # Real Network, Web, OSINT, Wireless, Crypto, Forensics, Custom executors
│       │       └── ui/                   # Compose Theme, Components, Navigation, ViewModel, 13 Screens
│       └── test/                         # Unit & Robolectric integration tests
├── docs/
│   ├── ARCHITECTURE.md
│   ├── SECURITY.md
│   ├── ANDROID_COMPATIBILITY.md
│   └── KNOWN_LIMITATIONS.md
├── THIRD_PARTY_NOTICES.md
└── gradle/libs.versions.toml
```

---

## Build & Test Instructions

### Local Project Setup (Optional)
You do **not** need to install Android Studio locally when building via GitHub Actions. For optional local CLI builds:
- **JDK 17** (`openjdk-17-jdk` / Eclipse Temurin 17)
- **Android SDK Platform 35** & **Build-Tools 35.0.0** (copy `local.properties.example` to `local.properties` and set `sdk.dir=...`)
- **Python 3.10** (used at build time by Chaquopy 16.0.0; pass `-Psword.buildPython=/path/to/python3.10` if multiple Python versions are installed)

Make the Gradle Wrapper executable on Linux/macOS if needed:
```bash
chmod +x ./gradlew
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest --no-daemon --max-workers=1 -Psword.buildPython=/usr/bin/python3.10
```

### Build Debug APK
```bash
./gradlew assembleDebug --no-daemon --max-workers=1 -Psword.buildPython=/usr/bin/python3.10
```
- **APK Output Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **SHA-256 Checksum**: generated in CI at `app/build/outputs/apk/debug/app-debug.apk.sha256`

---

## GitHub Actions Cloud Build (No Android Studio Required)

This repository includes an automated workflow at `.github/workflows/android-build.yml` (**Sword Android CI Build**) that compiles and tests the native Android application on GitHub-hosted `ubuntu-latest` runners.

### What the Workflow Does
1. Checks out the repository and configures **JDK 17** (Temurin) and **Python 3.10**.
2. Resolves and verifies the exact Python 3.10 executable path and passes it to Chaquopy via `-Psword.buildPython="$PYTHON310_BIN"`.
3. Installs **Android SDK Platform 35**, **Build Tools 35.0.0**, and `platform-tools`.
4. Enables Gradle caching via `gradle/actions/setup-gradle@v4` and limits memory usage with `--no-daemon --max-workers=1`.
5. Runs `./gradlew testDebugUnitTest` (uploads `sword-android-unit-test-reports` automatically if any test fails).
6. Builds the debug APK via `./gradlew assembleDebug`.
7. Verifies `app/build/outputs/apk/debug/app-debug.apk` exists and is non-empty, generates `app-debug.apk.sha256`, and uploads both in the `sword-android-debug-apk` artifact.

### How to Trigger a Build
- **Automatic**: Push commits or open a Pull Request targeting `main` or `master`.
- **Manual**:
  1. Open your repository on GitHub and click the **Actions** tab.
  2. Select **Sword Android CI Build** in the left sidebar.
  3. Click **Run workflow**, choose your branch, and click **Run workflow**.

### How to Download APK Artifacts
1. Open the **Actions** tab and click the completed **Sword Android CI Build** run.
2. Scroll down to the **Artifacts** section at the bottom of the summary page.
3. Click **`sword-android-debug-apk`** to download the ZIP containing:
   - `app-debug.apk`
   - `app-debug.apk.sha256`
4. Extract the ZIP and sideload `app-debug.apk` onto your Android device for testing.

### SHA-256 Verification
After extracting the downloaded artifact ZIP, verify the APK integrity against `app-debug.apk.sha256`:

- **Linux / macOS**:
  ```bash
  sha256sum -c app-debug.apk.sha256
  ```
- **Windows (PowerShell)**:
  ```powershell
  Get-FileHash .\app-debug.apk -Algorithm SHA256
  Get-Content .\app-debug.apk.sha256
  ```
  Confirm the computed hash matches the hex digest recorded in `app-debug.apk.sha256` (also printed in the workflow run's **Summary** tab).

### How to Inspect Failed Builds
1. Open the failed run in the GitHub **Actions** tab and click the **`Test & Build Debug APK`** job.
2. Expand the step marked with a red **X** (e.g. `Run Debug Unit Tests` or `Build Debug APK`) to view the full compiler/Gradle error output.
3. If unit tests failed, scroll to **Artifacts** and download **`sword-android-unit-test-reports`** (open `index.html` in a browser to inspect failing test stack traces).

---

## Future Release Signing Instructions

> **Security Notice**: Debug APKs (`app-debug.apk`) are signed with an auto-generated debug key and are intended **for testing only**. Do **not** distribute debug-signed APKs publicly, and **never** commit `.jks` / `.keystore` files, keystore passwords, API secrets, or private credentials to version control.

Release builds in `app/build.gradle.kts` already enable R8 code shrinking and resource shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`). When you are ready to produce signed release builds via GitHub Actions:

1. **Generate a private release keystore locally** (store it safely offline — never commit it):
   ```bash
   keytool -genkeypair -v -keystore sword-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sword-release
   ```
2. **Base64-encode the keystore** for GitHub Secrets:
   - Linux/macOS: `base64 -w 0 sword-release.jks` (macOS: `base64 -i sword-release.jks | tr -d '\n'`)
   - Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("sword-release.jks"))`
3. **Add Encrypted Repository Secrets** in GitHub (**Settings -> Secrets and variables -> Actions -> New repository secret**):
   - `ANDROID_KEYSTORE_BASE64` — Base64 string of `sword-release.jks`
   - `ANDROID_KEYSTORE_PASSWORD` — Keystore password
   - `ANDROID_KEY_ALIAS` — Key alias (e.g. `sword-release`)
   - `ANDROID_KEY_PASSWORD` — Key password
4. **Configure `signingConfigs` in `app/build.gradle.kts`** to read `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` from environment variables (or an untracked `keystore.properties` file), then decode `ANDROID_KEYSTORE_BASE64` to a temporary runner file during the release workflow step and run:
   ```bash
   ./gradlew assembleRelease --no-daemon --max-workers=1 -Psword.buildPython="$PYTHON310_BIN"
   ```
   (or `./gradlew bundleRelease` for Play Store `.aab` bundles), deleting the decoded temporary keystore in an `if: always()` cleanup step.

---

## Legal & Authorized Use

Sword Android is strictly intended for education, defensive diagnostics, and authorized security assessments against systems you own or have explicit permission to test.

