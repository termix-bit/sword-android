# Known Limitations

1. **Android Wi-Fi Scan Throttling & Location Dependency**
   - Since Android 9 (API 28), foreground apps are throttled to 4 Wi-Fi scans per 2-minute window (`WifiManager.startScan()`).
   - Android redacts SSID/BSSID and scan results unless both Location/Nearby Wi-Fi runtime permissions **and** the device's system Location toggle are enabled.

2. **Local Network Discovery Without Root**
   - Android 11+ restricts access to `/proc/net/arp`. Local LAN discovery uses bounded ICMP `isReachable` and common TCP port probes (`22`, `53`, `80`, `443`), so firewalled hosts that drop both ICMP and those ports may not appear.

3. **Raw Sockets & Monitor Mode**
   - Standard non-rooted Android devices prohibit `CAP_NET_RAW` and wireless monitor mode. Consequently, SYN scans, OS fingerprinting, raw packet injection, and Wi-Fi frame capture are unavailable.

4. **Embedded Python Engine Scope**
   - Chaquopy bundles Python 3.10 native libraries for `arm64-v8a`, `armeabi-v7a`, and `x86_64`, increasing APK size compared to a pure-Kotlin app.
   - To ensure build determinism and avoid native C-extension incompatibilities, bundled Python modules restrict themselves to the Python standard library.

5. **Device Emulator Verification**
   - In headless CI containers without hardware KVM or Android Emulator images, JVM unit tests and Robolectric tests validate business logic, Room persistence, MockWebServer HTTP auditing, and input validation, while physical-radio `WifiManager` scan behavior requires testing on physical Android hardware.
