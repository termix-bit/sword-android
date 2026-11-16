package com.sword.security.android.core.model

enum class CapabilityState(val label: String, val description: String) {
    AVAILABLE(
        label = "Available",
        description = "Fully supported on this device via native Android APIs or bundled runtime."
    ),
    AVAILABLE_WITH_LIMITATIONS(
        label = "Limited",
        description = "Supported with Android platform restrictions (e.g., throttling or non-root limits)."
    ),
    REQUIRES_PERMISSION(
        label = "Permission Required",
        description = "Requires runtime Android permission or device setting (e.g., Location) to run."
    ),
    UNSUPPORTED(
        label = "Unsupported",
        description = "Blocked by Android security model or missing hardware capability."
    ),
    NOT_IMPLEMENTED(
        label = "Not Implemented",
        description = "Excluded by Sword security policy or planned for a future release."
    )
}
