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

### Prerequisites
- JDK 17 (`openjdk-17-jdk`)
- Android SDK Platform 35 & Build-Tools 35.0.0 (`local.properties` -> `sdk.dir=...`)
- Python 3.10 (for Chaquopy build-time packaging)

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build & Signing Procedure
Release builds enable R8 code shrinking and resource shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`).
Do **not** commit `.jks` keystores or passwords to version control. Configure signing locally via `keystore.properties` or CI environment variables:
1. Generate a release upload key using `keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sword-release`.
2. Provide `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` via secure CI secrets or an ignored `keystore.properties` file.
3. Build the release bundle or APK with `./gradlew bundleRelease` or `./gradlew assembleRelease`.

---

## Legal & Authorized Use

Sword Android is strictly intended for education, defensive diagnostics, and authorized security assessments against systems you own or have explicit permission to test.
