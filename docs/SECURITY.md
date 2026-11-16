# Sword Android Security Model

Security controls are enforced across every architectural boundary:

1. **Zero Arbitrary Command / Shell Execution**
   - Unlike Linux/Termux CLI wrappers, Sword Android never spawns `sh -c`, `ProcessBuilder` with user input, or arbitrary `eval()`/`exec()`.
   - All network, TLS, DNS, cryptography, and forensics operations run through strongly typed Kotlin APIs or pre-audited Chaquopy Python functions.

2. **Strict Input Validation (`InputValidator`)**
   - IPv4/IPv6 addresses, CIDR prefixes (`/22`–`/30` bounded), port lists (max 64 ports per probe), HTTP/HTTPS URLs (rejecting embedded `user:pass@` credentials and non-HTTP schemes), domain labels, usernames, hex strings, and proxy URLs are validated before execution.

3. **Transport & Cleartext Protection**
   - `android:usesCleartextTraffic="false"` is enforced in `AndroidManifest.xml`.
   - All OSINT and DoH lookups use HTTPS with system X.509 trust validation (fixing the legacy `http://ip-api.com` cleartext call found in the reference repo).
   - Network timeouts and optional OkHttp proxy routing are centrally enforced via `HttpClientFactory`.

4. **Data Privacy & App-Private Storage**
   - `android:allowBackup="false"` prevents automatic cloud/ADB extraction of local scan reports.
   - Reports are stored strictly inside `context.filesDir/reports/` with canonical path traversal checks (`candidate.parentFile == dir`) and shared only through scoped `FileProvider` URIs or explicit user SAF `CreateDocument` actions.
   - Forensics tools read files strictly via user-initiated Android Storage Access Framework (`ACTION_OPEN_DOCUMENT`) without requesting broad `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE`.

5. **Authorized Testing & Exclusion of Offensive Tools**
   - Potentially active checks (port probes, LAN discovery, HTTP audits, multi-site OSINT) display an explicit authorization confirmation dialog when `requireAuthorizationConfirmation` is enabled.
   - Deauthentication attacks, evil-twin APs, credential theft, stealth persistence, SQLMap, and directory bruteforcing are intentionally excluded.

6. **Release Signing Procedure**
   - Keep signing keystores (`*.jks`, `*.keystore`) and `keystore.properties` untracked in `.gitignore`.
   - Supply signing credentials only at build time via CI secrets or local ignored properties.
