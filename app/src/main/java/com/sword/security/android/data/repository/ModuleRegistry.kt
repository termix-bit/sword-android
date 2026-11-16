package com.sword.security.android.data.repository

import android.Manifest
import android.os.Build
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.ParameterType
import com.sword.security.android.core.model.SwordCompatibility
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.core.model.ToolParameter

object ModuleRegistry {

    val allTools: List<ToolDescriptor> = listOf(
        // === NETWORK ===
        ToolDescriptor(
            id = "net_interfaces",
            name = "Network Interface Inspector",
            category = ModuleCategory.NETWORK,
            summary = "Inspect active interfaces, IPv4/IPv6 addresses, DNS, and MTU.",
            description = "Enumerates active Android LinkProperties and Java NetworkInterfaces without requiring Termux or root.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces Termux ip/ifconfig checks with Android ConnectivityManager & NetworkInterface APIs."
        ),
        ToolDescriptor(
            id = "net_dns_lookup",
            name = "DNS Record Lookup (DoH)",
            category = ModuleCategory.NETWORK,
            summary = "Encrypted DNS-over-HTTPS query for A, AAAA, MX, TXT, NS, CNAME, SOA.",
            description = "Queries Cloudflare, Google, or Quad9 DoH JSON endpoints to inspect authoritative DNS records.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Replaces Termux 'dig' / 'nslookup' CLI tools with encrypted RFC 8484 DNS-over-HTTPS.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true),
                ToolParameter(
                    "record_type",
                    "Record Type",
                    "A",
                    ParameterType.SELECT,
                    required = true,
                    defaultValue = "A",
                    options = listOf("A", "AAAA", "MX", "TXT", "NS", "CNAME", "SOA")
                )
            )
        ),
        ToolDescriptor(
            id = "net_connectivity",
            name = "Connectivity & Latency Diagnostics",
            category = ModuleCategory.NETWORK,
            summary = "Benchmark DNS resolution, reachability, and TCP handshake latency.",
            description = "Measures DNS timing, best-effort reachability, and 3-sample TCP connect RTT against an authorized host.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces raw ping/traceroute binaries with Android-compatible DNS + TCP RTT diagnostics.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("host", "Target Host or IP", "example.com", ParameterType.TEXT, required = true),
                ToolParameter("port", "TCP Port", "443", ParameterType.INTEGER, required = true, defaultValue = "443")
            )
        ),
        ToolDescriptor(
            id = "net_port_probe",
            name = "Authorized TCP Port Probe",
            category = ModuleCategory.NETWORK,
            summary = "Bounded coroutine TCP connect scan (up to 64 ports) for authorized targets.",
            description = "Tests TCP reachability across a specified port list or small range with concurrency limits and cancellation.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Replaces unprivileged Termux nmap TCP connect checks; raw SYN/OS fingerprinting requires root and is excluded.",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            staticLimitationReason = "Limited to 64 ports per run using standard TCP connect() sockets.",
            parameters = listOf(
                ToolParameter("host", "Authorized Target Host/IP", "192.168.1.1", ParameterType.TEXT, required = true),
                ToolParameter(
                    "ports",
                    "Ports or Range (max 64)",
                    "22,80,443,8080,8443",
                    ParameterType.PORT_LIST,
                    required = true,
                    defaultValue = "22,53,80,443,8080,8443"
                )
            )
        ),
        ToolDescriptor(
            id = "net_lan_discovery",
            name = "Authorized Local LAN Discovery",
            category = ModuleCategory.NETWORK,
            summary = "Discover responsive hosts on your active local /24 IPv4 subnet.",
            description = "Probes local subnet hosts using ICMP reachability and common LAN service ports (22, 53, 80, 443).",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Replaces 'nmap -sn' on Termux; leaves ARP table access limitations intact on modern Android.",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            staticLimitationReason = "Android 11+ restricts /proc/net/arp; relies on active ICMP/TCP reachability probes.",
            parameters = listOf(
                ToolParameter(
                    "subnet",
                    "IPv4 Subnet CIDR (blank = auto-detect local Wi-Fi)",
                    "192.168.1.0/24",
                    ParameterType.SUBNET,
                    required = false,
                    defaultValue = ""
                )
            )
        ),
        ToolDescriptor(
            id = "net_raw_packets_unsupported",
            name = "Raw Packet Crafting (hping3 / SYN)",
            category = ModuleCategory.NETWORK,
            summary = "Unsupported on standard non-rooted Android devices.",
            description = "Custom raw socket injection (hping3, nmap -O, nmap -sS) requires CAP_NET_RAW privileges blocked by Android SELinux.",
            compatibility = SwordCompatibility.UNSUPPORTED_ON_ANDROID,
            compatibilityNotes = "Legacy Sword Linux/Termux root tool; unavailable in standard Android sandbox.",
            isExecutable = false
        ),

        // === WEB SECURITY ===
        ToolDescriptor(
            id = "web_header_audit",
            name = "HTTP Security Header Analyzer",
            category = ModuleCategory.WEB_SECURITY,
            summary = "Inspect HTTP status, redirects, HSTS, CSP, cookies, and server headers.",
            description = "Fetches response headers over TLS and flags missing HSTS, CSP, X-Frame-Options, nosniff, and insecure cookies.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces Termux 'curl -I' with structured security header scoring and remediation findings.",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            parameters = listOf(
                ToolParameter("url", "Target URL", "https://example.com", ParameterType.URL, required = true)
            )
        ),
        ToolDescriptor(
            id = "web_tls_inspector",
            name = "TLS / X.509 Certificate Inspector",
            category = ModuleCategory.WEB_SECURITY,
            summary = "Inspect TLS protocol, cipher suite, SANs, issuer, and certificate expiry.",
            description = "Performs a verified SNI TLS handshake and inspects the peer X.509 certificate chain.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Upgrades Sword Python ssl.getpeercert() with full cipher, SAN, and expiry analysis.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true),
                ToolParameter("port", "TLS Port", "443", ParameterType.INTEGER, required = true, defaultValue = "443")
            )
        ),
        ToolDescriptor(
            id = "web_config_check",
            name = "Authorized Site Config Check",
            category = ModuleCategory.WEB_SECURITY,
            summary = "Inspect /.well-known/security.txt and /robots.txt over HTTPS.",
            description = "Performs passive configuration checks for RFC 9116 security disclosure metadata and exposed robots hints.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Safe native replacement for passive reconnaissance; intrusive bruteforce tools (sqlmap/ffuf) are excluded.",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true)
            )
        ),

        // === OSINT ===
        ToolDescriptor(
            id = "osint_rdap_whois",
            name = "Domain RDAP / WHOIS Lookup",
            category = ModuleCategory.OSINT,
            summary = "Retrieve registrar status, nameservers, and lifecycle dates via HTTPS RDAP.",
            description = "Queries rdap.org over TLS instead of cleartext port-43 WHOIS binaries.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Replaces Termux 'whois' binary with structured HTTPS RDAP JSON queries.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true)
            )
        ),
        ToolDescriptor(
            id = "osint_ip_intel",
            name = "Public IP & ASN Intelligence",
            category = ModuleCategory.OSINT,
            summary = "Look up ASN, ISP, organization, and geolocation over HTTPS.",
            description = "Queries ipwho.is over HTTPS (fixing the cleartext HTTP preset present in legacy Sword).",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Migrated from Sword OSINT module; upgraded to strict HTTPS-only transport.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("ip", "Public IPv4 or IPv6", "1.1.1.1", ParameterType.IP_ADDRESS, required = true)
            )
        ),
        ToolDescriptor(
            id = "osint_dns_posture",
            name = "Domain DNS & Mail Posture",
            category = ModuleCategory.OSINT,
            summary = "Audit MX, SPF (v=spf1), and DMARC (_dmarc) TXT records via DoH.",
            description = "Combines encrypted DoH lookups to identify missing SPF or DMARC anti-spoofing policies.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Native Android expansion of Sword DNS OSINT checks.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true)
            )
        ),
        ToolDescriptor(
            id = "osint_username_check",
            name = "Username Availability (Python Engine)",
            category = ModuleCategory.OSINT,
            summary = "Check username presence across public platforms via embedded Python 3.10.",
            description = "Uses Sword's WhatsMyName dataset (CC BY-SA 4.0) with wildcard false-positive checks and strict rate limits.",
            compatibility = SwordCompatibility.REUSED_PYTHON,
            compatibilityNotes = "Reuses Sword tools/username_scanner.py & wmn-data.json inside Chaquopy Python 3.10 (stdlib urllib).",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            staticLimitationReason = "Capped at 5-50 sites per run to protect mobile battery and prevent rate-limit blocking.",
            parameters = listOf(
                ToolParameter("username", "Username", "octocat", ParameterType.USERNAME, required = true),
                ToolParameter("max_sites", "Max Sites (5-50)", "25", ParameterType.INTEGER, required = true, defaultValue = "25")
            )
        ),

        // === WIRELESS INFORMATION ===
        ToolDescriptor(
            id = "wifi_connection_info",
            name = "Active Wi-Fi Connection Info",
            category = ModuleCategory.WIRELESS,
            summary = "Inspect SSID/BSSID (when permitted), frequency band, RSSI, speed, and DNS.",
            description = "Reads active wireless diagnostics from Android WifiManager and ConnectivityManager.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces 'termux-wifi-connectioninfo' subprocess calls with native Android system services.",
            requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.ACCESS_WIFI_STATE)
            } else {
                listOf(Manifest.permission.ACCESS_WIFI_STATE)
            }
        ),
        ToolDescriptor(
            id = "wifi_nearby_scan",
            name = "Nearby Wi-Fi Access Points",
            category = ModuleCategory.WIRELESS,
            summary = "Inspect visible Wi-Fi networks, channels, signal levels, and security capabilities.",
            description = "Uses Android WifiManager scan APIs to detect open or legacy WEP networks nearby.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces 'termux-wifi-scaninfo'; subject to Android 9+ scan throttling (4 scans / 2 min) & Location toggle.",
            staticLimitationReason = "Requires Location/Nearby Wi-Fi permission and Location Services enabled on device."
        ),
        ToolDescriptor(
            id = "wifi_monitor_deauth_unsupported",
            name = "Monitor Mode & Packet Injection",
            category = ModuleCategory.WIRELESS,
            summary = "Unsupported by Android hardware/OS & excluded by Sword security policy.",
            description = "Standard Android Wi-Fi firmware does not support monitor mode; deauthentication & evil-twin attacks are excluded.",
            compatibility = SwordCompatibility.UNSUPPORTED_ON_ANDROID,
            compatibilityNotes = "Marked unsupported in Sword Android to respect OS limits and authorized-use security boundaries.",
            isExecutable = false
        ),

        // === CRYPTOGRAPHY ===
        ToolDescriptor(
            id = "crypto_hash_gen",
            name = "Cryptographic Hash Generator",
            category = ModuleCategory.CRYPTOGRAPHY,
            summary = "Compute SHA-256, SHA-512, and legacy SHA-1/MD5 digests.",
            description = "Computes strong SHA-256/512 digests and marks MD5/SHA-1 as legacy reference only.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Upgrades Sword modules/crypto with SHA-512 and explicit weak-hash warnings.",
            parameters = listOf(
                ToolParameter("text", "Input Text", "Enter text to hash", ParameterType.TEXT, required = true)
            )
        ),
        ToolDescriptor(
            id = "crypto_hash_verify",
            name = "Constant-Time Hash Verifier",
            category = ModuleCategory.CRYPTOGRAPHY,
            summary = "Verify text against an expected SHA-256 or SHA-512 checksum.",
            description = "Uses MessageDigest.isEqual for constant-time digest comparison.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "New Android-native integrity verification capability.",
            parameters = listOf(
                ToolParameter("text", "Input Text", "Original content", ParameterType.TEXT, required = true),
                ToolParameter("expected_hash", "Expected Hex Digest", "64 or 128 hex chars", ParameterType.TEXT, required = true),
                ToolParameter(
                    "algorithm",
                    "Algorithm",
                    "SHA-256",
                    ParameterType.SELECT,
                    required = true,
                    defaultValue = "SHA-256",
                    options = listOf("SHA-256", "SHA-512")
                )
            )
        ),
        ToolDescriptor(
            id = "crypto_codec",
            name = "Encoding & Cipher Utility",
            category = ModuleCategory.CRYPTOGRAPHY,
            summary = "Base64, Hex, URL encode/decode, and ROT13 educational transform.",
            description = "Clearly distinguishes reversible encodings from cryptographic encryption.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Migrated from Sword modules/crypto with added Hex and URL transforms.",
            parameters = listOf(
                ToolParameter("text", "Input Text", "Enter text or encoded value", ParameterType.TEXT, required = true),
                ToolParameter(
                    "mode",
                    "Operation",
                    "Base64 Encode",
                    ParameterType.SELECT,
                    required = true,
                    defaultValue = "Base64 Encode",
                    options = listOf(
                        "Base64 Encode",
                        "Base64 Decode",
                        "Hex Encode",
                        "Hex Decode",
                        "URL Encode",
                        "URL Decode",
                        "ROT13"
                    )
                )
            )
        ),
        ToolDescriptor(
            id = "crypto_random_gen",
            name = "Secure Random Token Generator",
            category = ModuleCategory.CRYPTOGRAPHY,
            summary = "Generate CSPRNG Hex, Base64URL secrets, and UUID v4 via SecureRandom.",
            description = "Generates high-entropy cryptographic tokens using Android's SecureRandom provider.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "New Android-specific security utility.",
            parameters = listOf(
                ToolParameter("bytes", "Entropy Size (8-128 bytes)", "32", ParameterType.INTEGER, required = true, defaultValue = "32")
            )
        ),

        // === FORENSICS ===
        ToolDescriptor(
            id = "forensics_file_inspect",
            name = "File Signature & Evidence Hasher",
            category = ModuleCategory.FORENSICS,
            summary = "Identify file magic bytes, compute SHA-256/512, and record chain-of-custody.",
            description = "Inspects any user-selected file via Android SAF without broad storage permissions and saves an evidence record.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Replaces Linux 'file' command with native magic-byte detection + Room evidence persistence.",
            parameters = listOf(
                ToolParameter("file_uri", "Select File (SAF Document Picker)", "Tap Pick File", ParameterType.FILE_URI, required = true)
            )
        ),
        ToolDescriptor(
            id = "forensics_exif_audit",
            name = "Image EXIF Metadata Audit",
            category = ModuleCategory.FORENSICS,
            summary = "Inspect camera model, timestamps, software tags, and GPS coordinates.",
            description = "Uses AndroidX ExifInterface to detect privacy-sensitive GPS coordinates and device metadata in images.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces Termux 'exiftool' dependency with AndroidX ExifInterface.",
            parameters = listOf(
                ToolParameter("file_uri", "Select Image File", "Tap Pick File", ParameterType.FILE_URI, required = true)
            )
        ),
        ToolDescriptor(
            id = "forensics_strings_hex",
            name = "Safe Hex Dump & Strings Extractor",
            category = ModuleCategory.FORENSICS,
            summary = "Extract bounded printable ASCII strings and 320-byte hex dump.",
            description = "Inspects up to 256 KB safely in memory without requiring Linux 'xxd' or 'strings' binaries.",
            compatibility = SwordCompatibility.NATIVE_ANDROID,
            compatibilityNotes = "Replaces Termux 'xxd' and 'strings' binaries with memory-bounded Kotlin streaming.",
            staticLimitationReason = "Inspection window capped at 256 KB (and 80 strings) to keep mobile UI responsive.",
            parameters = listOf(
                ToolParameter("file_uri", "Select File", "Tap Pick File", ParameterType.FILE_URI, required = true),
                ToolParameter("min_length", "Minimum String Length (4-24)", "6", ParameterType.INTEGER, required = true, defaultValue = "6")
            )
        ),

        // === CUSTOM TOOLS ===
        ToolDescriptor(
            id = "custom_web_audit",
            name = "Full Website Audit Preset",
            category = ModuleCategory.CUSTOM_TOOLS,
            summary = "4-step authorized audit: TLS, HTTP headers, DNS posture, and security.txt.",
            description = "Native Android re-engineering of Sword's Full Website Audit preset with structured multi-stage reporting.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Migrated from Sword Settings Preset #7; replaces curl/whatweb/dig with native TLS/HTTP/DoH checks.",
            requiresNetwork = true,
            requiresAuthorizationConfirmation = true,
            parameters = listOf(
                ToolParameter("domain", "Authorized Target Domain", "example.com", ParameterType.DOMAIN, required = true)
            )
        ),
        ToolDescriptor(
            id = "custom_domain_osint",
            name = "Domain OSINT Profile Preset",
            category = ModuleCategory.CUSTOM_TOOLS,
            summary = "3-step intelligence profile: RDAP WHOIS, A/NS DoH records, and SPF/DMARC.",
            description = "Combines passive HTTPS domain intelligence checks into a single unified report.",
            compatibility = SwordCompatibility.SUBSTANTIALLY_MODIFIED,
            compatibilityNotes = "Migrated from Sword OSINT Profile preset with strict HTTPS transport.",
            requiresNetwork = true,
            parameters = listOf(
                ToolParameter("domain", "Domain Name", "example.com", ParameterType.DOMAIN, required = true)
            )
        ),
        ToolDescriptor(
            id = "custom_python_runner",
            name = "Sandboxed Python 3.10 Bridge",
            category = ModuleCategory.CUSTOM_TOOLS,
            summary = "Run pre-audited Chaquopy Python 3.10 validators and crypto helpers.",
            description = "Executes bundled pure-Python Sword modules safely inside the APK without arbitrary eval/exec or shell access.",
            compatibility = SwordCompatibility.REUSED_PYTHON,
            compatibilityNotes = "Replaces arbitrary custom_scripts/*.py subprocess execution with safe, non-eval Chaquopy entrypoints.",
            parameters = listOf(
                ToolParameter(
                    "operation",
                    "Sandboxed Python Operation",
                    "Runtime Health Check",
                    ParameterType.SELECT,
                    required = true,
                    defaultValue = "Runtime Health Check",
                    options = listOf(
                        "Runtime Health Check",
                        "Validate Domain (Python)",
                        "Validate URL (Python)",
                        "Compute Hashes (Python)"
                    )
                ),
                ToolParameter("input", "Input Value (for validate/hash ops)", "", ParameterType.TEXT, required = false, defaultValue = "")
            )
        )
    )

    fun getToolsByCategory(category: ModuleCategory): List<ToolDescriptor> =
        allTools.filter { it.category == category }

    fun findTool(id: String): ToolDescriptor? =
        allTools.firstOrNull { it.id == id }
}
