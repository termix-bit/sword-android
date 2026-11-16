package com.sword.security.android.core.model

enum class ModuleCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val isToolCategory: Boolean = true
) {
    NETWORK("network", "Network", "Interfaces, DNS, connectivity & authorized LAN probes"),
    WEB_SECURITY("web", "Web Security", "HTTP headers, TLS certificates & configuration audits"),
    OSINT("osint", "OSINT", "RDAP WHOIS, IP intelligence, DNS posture & usernames"),
    WIRELESS("wireless", "Wireless Info", "Wi-Fi connection & nearby AP diagnostics"),
    CRYPTOGRAPHY("crypto", "Cryptography", "SHA-256/512 hashing, verification, encoding & CSPRNG"),
    FORENSICS("forensics", "Forensics", "File signatures, EXIF audit, strings & hex dump"),
    REPORTS("reports", "Reports", "JSON/HTML security reports & export", isToolCategory = false),
    CUSTOM_TOOLS("custom", "Custom Tools", "Multi-step presets & sandboxed Python inspector"),
    SETTINGS("settings", "Settings", "Preferences, timeouts, proxy & safety controls", isToolCategory = false)
}
