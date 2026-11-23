# ⚔️ Sword Android — Native Android Security Toolkit

<p align="center">
  <strong>A standalone Android cybersecurity toolkit.</strong>
  <br />
  Built with Kotlin, Jetpack Compose, and embedded Python.
</p>

<p align="center">
  <a href="https://github.com/termix-bit/sword-android/releases/latest">
    <img src="https://img.shields.io/github/v/release/termix-bit/sword-android?label=Latest%20Release&color=8A63D2" alt="Latest Release" />
  </a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Python-3.10-3776AB?logo=python&logoColor=white" alt="Python 3.10" />
  <img src="https://img.shields.io/badge/Status-First%20Release-8A63D2" alt="Release status" />
</p>

---

## Overview

**Sword Android** is an independent, native Android cybersecurity diagnostic, OSINT, cryptography, and digital forensics toolkit.

Built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, OkHttp, and an embedded Python 3.10 runtime powered by Chaquopy.

Sword Android is designed as a standalone application. It does not require Termux, a separately installed Python environment, or a command-line setup.

It is not a WebView wrapper or a Termux frontend. The application is built around Android's native APIs, security sandbox, Scoped Storage, and permission model.

### Sword Product Family

Sword is being developed as a family of independent security tools:

| Project | Platform | Description |
|---|---|---|
| [Sword Termux](https://github.com/termix-bit/sword) | Android / Termux | Modular terminal-based security framework |
| **Sword Android** | Android | Standalone native Android security toolkit |
| Sword Linux | Linux | Dedicated desktop and server security suite — in development |

---

## Download

### Latest Release — v1.0.0

<p align="center">
  <a href="https://github.com/termix-bit/sword-android/releases/latest">
    <img src="https://img.shields.io/badge/Download-Sword%20Android%20v1.0.0-8A63D2?style=for-the-badge&logo=android&logoColor=white" alt="Download Sword Android" />
  </a>
</p>

**Download page:**  
https://github.com/termix-bit/sword-android/releases/latest

To install:

1. Open the Releases page.
2. Download the `app-release.apk` asset.
3. Open the downloaded APK on your Android device.
4. If prompted, allow your browser or file manager to install unknown apps.
5. Complete installation and launch Sword Android.

> Download APK files only from the official Sword Android GitHub Releases page.

### Verify the Download

Each release includes a SHA-256 checksum file.

To verify the downloaded APK on Windows PowerShell:

```powershell
Get-FileHash .\app-release.apk -Algorithm SHA256
```

Compare the resulting hash with the value in `app-release.apk.sha256`.

On Linux:

```bash
sha256sum -c app-release.apk.sha256
```

---

## Android Compatibility

| Requirement | Configuration |
|---|---|
| Minimum Android version | Android 8.0 Oreo |
| Minimum SDK | API 26 |
| Compile SDK | API 35 |
| Target SDK | API 35 |
| Embedded Python | Python 3.10 |
| Java target | Java 17 |

### Supported CPU Architectures

- `arm64-v8a`
- `armeabi-v7a`
- `x86_64`

Android versions newer than Android 15 may also run the application. Compatibility should be verified on actual devices.

`targetSdk 35` does not impose Android 15 as the maximum supported version.

---

## Core Capabilities

### Network
- Network interface and `LinkProperties` inspection.
- DNS-over-HTTPS lookups using RFC 8484.
- DNS record inspection: A, AAAA, MX, TXT, NS, CNAME, and SOA.
- Connectivity and TCP RTT latency benchmarking.
- Bounded authorized TCP port probing (up to 64 ports).
- Authorized local `/24` LAN discovery.

### Web Security
- HTTP security header analysis:
  - HSTS
  - CSP
  - X-Frame-Options
  - X-Content-Type-Options
  - Cookie security flags
- X.509 TLS certificate inspection.
- TLS cipher inspection.
- Passive website configuration checks:
  - `/.well-known/security.txt`
  - `/robots.txt`

### OSINT
- HTTPS RDAP/WHOIS domain lookup.
- IP address and ASN intelligence.
- SPF, DMARC, and MX email DNS posture checks.
- Rate-limited username availability checks.
- Bundled WhatsMyName dataset.

### Wireless Information
- Active Wi-Fi connection diagnostics.
- Nearby access point security scanning.
- Android Wi-Fi and Location permission transparency.
- Permission-aware operation and throttling awareness.

### Cryptography
- SHA-256 and SHA-512 digest generation.
- Legacy MD5 and SHA-1 identification.
- Constant-time hash verification.
- Base64, Hex, URL, and ROT13 utilities.
- SecureRandom token generation.

### Forensics
- Android Storage Access Framework file inspection.
- File metadata and magic-byte signature detection.
- Streaming SHA-256 and SHA-512 hashing.
- Chain-of-custody hash persistence.
- EXIF GPS and device privacy inspection.
- Memory-bounded hex dump and printable string extraction.

### Reports and Persistence
- Room database for scan history and report metadata.
- Forensics evidence records and activity logs.
- App-private JSON report generation.
- Sword-branded HTML reports.
- Storage Access Framework export.
- FileProvider sharing.
- Individual report deletion.

### Device Capabilities and Permissions
- Android API level detection.
- Network transport and VPN state inspection.
- Location Services status.
- Runtime permission monitoring.
- Optional root binary presence detection.
- Embedded Python runtime health checks.

---

## Architecture and Technology

Sword Android uses a native Android architecture with a Kotlin application layer and an embedded Python runtime.

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design system | Material 3 |
| Local database | Room |
| Preferences | DataStore |
| Networking | OkHttp |
| Python integration | Chaquopy |
| Embedded Python | Python 3.10 |
| Serialization | Kotlinx Serialization |
| Background tasks | WorkManager |
| Image metadata | AndroidX ExifInterface |
| Build system | Gradle |
| Code shrinking | R8 / ProGuard |

### Project Structure

```text
Sword-Android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── python/sword_py/
│       │   ├── res/
│       │   └── java/com/sword/security/android/
│       │       ├── core/
│       │       ├── data/
│       │       ├── domain/tools/
│       │       └── ui/
│       └── test/
├── docs/
│   ├── ARCHITECTURE.md
│   ├── SECURITY.md
│   ├── ANDROID_COMPATIBILITY.md
│   └── KNOWN_LIMITATIONS.md
├── THIRD_PARTY_NOTICES.md
└── gradle/
    └── libs.versions.toml
```

---

## Build and Development

### Requirements

For local development:

- JDK 17
- Android SDK Platform 35
- Android Build Tools 35.0.0
- Python 3.10
- Gradle Wrapper included in the repository

Android Studio is optional. The project can also be built using Gradle from the command line.

### Run Unit Tests

Linux/macOS:

```bash
./gradlew testDebugUnitTest \
  --no-daemon \
  --max-workers=1 \
  -Psword.buildPython=/usr/bin/python3.10
```

Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest --no-daemon --max-workers=1
```

### Build Debug APK

```bash
./gradlew assembleDebug \
  --no-daemon \
  --max-workers=1 \
  -Psword.buildPython=/usr/bin/python3.10
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

Release builds use a private signing key supplied through environment variables.

```bash
./gradlew assembleRelease \
  --no-daemon \
  --max-workers=1 \
  -Psword.buildPython=/usr/bin/python3.10
```

Output:

```text
app/build/outputs/apk/release/app-release.apk
```

A valid release signing configuration is required for a signed release APK.

---

## GitHub Actions and Automated Releases

Sword Android uses GitHub Actions to automate testing, APK builds, and release publishing.

### CI Workflow

On pushes to `main` or `master`, and on pull requests targeting those branches, the workflow:

1. Configures JDK 17.
2. Configures Python 3.10.
3. Installs Android SDK Platform 35 and Build Tools 35.0.0.
4. Runs Debug unit tests.
5. Builds the Debug APK.
6. Generates a SHA-256 checksum.
7. Uploads the Debug APK as a workflow artifact.

### Release Workflow

Pushing a version tag matching `v*` triggers the release job.

The release job:

1. Checks out the tagged source revision.
2. Configures JDK 17, Python 3.10, and Android SDK.
3. Decodes the private release keystore from GitHub Actions Secrets into a temporary runner file.
4. Runs unit tests.
5. Builds the signed Release APK.
6. Verifies the APK signature.
7. Generates a SHA-256 checksum.
8. Publishes the APK and checksum to GitHub Releases.

### Release Signing Secrets

The following GitHub repository secrets are required:

| Secret | Purpose |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded private keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Signing key alias |
| `ANDROID_KEY_PASSWORD` | Signing key password |

The private keystore and passwords must never be committed to source control.

### Publish a New Version

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Commit and push the changes to `main`.
3. Create a version tag, for example `v1.0.1`.
4. Push the tag to GitHub.
5. GitHub Actions builds and publishes the new release.

Release page:

https://github.com/termix-bit/sword-android/releases

---

## Security and Responsible Use

Sword Android is intended for educational purposes, defensive diagnostics, and authorized security assessments.

Only test systems, applications, networks, and devices that you own or have explicit permission to assess.

### Android Permissions

Some features require Android runtime permissions or enabled system services.

Wireless scanning behavior depends on Android version, device manufacturer, Wi-Fi hardware, Location Services, and granted permissions.

### Root Access

Root access is not required for the application's basic features.

The application can detect the presence of root-related binaries, but their presence does not mean all privileged operations are supported.

### Privacy

Some OSINT and network features communicate with external services.

Users should review the relevant service terms and avoid submitting sensitive information without authorization.

Scan reports may contain network addresses, targets, and collected metadata. Treat exported reports as potentially sensitive.

---

## Legal Notice

Sword Android is a security education and authorized assessment toolkit.

The user is responsible for obtaining appropriate permission before performing security checks against third-party systems.

The developers are not responsible for misuse or unauthorized activity.

---

## Contributing

Bug reports, documentation improvements, and pull requests are welcome.

When reporting issues, include:

- Android version.
- Device architecture.
- Relevant application version.
- Steps to reproduce the issue.
- Relevant error logs.

Remove credentials, tokens, private IP information, and other sensitive data before sharing logs.

---

## License

See the repository's `LICENSE` file for licensing terms.

Third-party libraries, tools, and datasets remain subject to their respective licenses and terms. See `THIRD_PARTY_NOTICES.md`.

---

<p align="center">
  <strong>⚔️ Sword Android</strong>
  <br />
  <sub>Independent. Native. Modular.</sub>
  <br />
  <a href="https://github.com/termix-bit/sword-android/releases/latest">Download</a>
  ·
  <a href="https://github.com/termix-bit/sword-android">Source Code</a>
  ·
  <a href="https://github.com/termix-bit/sword-android/issues">Issues</a>
</p>
