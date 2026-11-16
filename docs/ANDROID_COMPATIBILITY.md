# Sword Reference Compatibility Inventory

Analyzed read-only against `termix-bit/sword`.

## 1. Directly Reusable Python Logic
- `core/validators.py` -> Adapted into `sword_py/validators.py` (IP, subnet, port, URL, domain, username validation) and mirrored natively in `InputValidator.kt`.
- `tools/username_scanner.py` + `tools/wmn-data.json` -> Adapted into `sword_py/username_engine.py` using pure Python stdlib (`urllib.request`, `ssl`, `ThreadPoolExecutor`) with wildcard false-positive mitigation preserved.
- `modules/crypto` basic hashing/ROT13/Base64 logic -> Reference algorithms reused in `sword_py/crypto_utils.py` and expanded natively in `CryptoToolsExecutor.kt`.

## 2. Requires Android-Native Implementation
- `modules/wireless` (`termux-wifi-connectioninfo`, `termux-wifi-scaninfo`) -> Replaced by native `WifiManager`, `ConnectivityManager`, `NetworkCapabilities`, and `PermissionManager` (`WirelessToolsExecutor.kt`).
- `modules/forensics` (`exiftool`, `file`, `strings`, `xxd`) -> Replaced by AndroidX `ExifInterface`, magic-byte signature detection, memory-bounded Kotlin streaming hex/strings extractor, and Android SAF (`ForensicsToolsExecutor.kt`).
- `core/report_server.py` (`127.0.0.1` HTTP server + `termux-open-url`) -> Replaced by app-private `ReportStorageManager`, Room `ReportMetadataEntity`, `FileProvider`, and Android SAF `CreateDocument` export.
- `core/settings_manager.py` -> Replaced by Jetpack `DataStore` (`UserPreferencesRepository`).

## 3. Requires Substantial Modification
- `modules/network` (`nmap`, `dig`, `ping`, `traceroute`, `rustscan`) -> Replaced by `NetworkInterface` enumeration, RFC 8484 DNS-over-HTTPS, TCP RTT connect benchmark, bounded coroutine TCP port probe, and local `/24` LAN reachability probe.
- `modules/web` (`curl -I`, `nslookup`, `ssl.getpeercert`) -> Upgraded to OkHttp security header scoring (`HSTS`, `CSP`, `X-Frame-Options`, `nosniff`, cookie flags), full SNI X.509 TLS inspector, and passive `.well-known/security.txt` / `robots.txt` check.
- `modules/osint` (`whois`, `nslookup`, `ip-api.com`) -> Replaced with HTTPS RDAP (`rdap.org`), HTTPS IP intelligence (`ipwho.is`), and DoH SPF/DMARC posture checks.
- `modules/custom` (`custom_scripts/*.py` arbitrary subprocess) -> Replaced with curated multi-step audit presets and sandboxed non-eval Chaquopy diagnostic calls.

## 4. Unsupported by Android (or Excluded by Policy)
- `hping3` custom raw packet crafting (`CAP_NET_RAW` blocked in Android sandbox).
- `nmap -O` / `nmap -sS` raw SYN & OS fingerprinting (requires root + raw sockets).
- Wireless monitor mode & deauth attacks (`iw`/`aireplay-ng`) — unsupported on standard Android Wi-Fi firmware and excluded by Sword Android's authorized/defensive security policy.
- Offensive web scanners (`sqlmap`, `ffuf`) and Termux self-update (`git pull`) — excluded in standalone native APK distribution.

## 5. New Android-Specific Functionality
- Centralized `CapabilityManager` with 5 explicit capability states (`AVAILABLE`, `AVAILABLE_WITH_LIMITATIONS`, `REQUIRES_PERMISSION`, `UNSUPPORTED`, `NOT_IMPLEMENTED`).
- Dedicated `PermissionsManagerScreen` with API-level-aware runtime permission requests (`NEARBY_WIFI_DEVICES` vs `ACCESS_FINE_LOCATION`).
- Room-backed `EvidenceRecordEntity` chain-of-custody storage and `ScanHistoryScreen`.
- `SecureRandom` CSPRNG token generator and constant-time (`MessageDigest.isEqual`) hash verifier.
