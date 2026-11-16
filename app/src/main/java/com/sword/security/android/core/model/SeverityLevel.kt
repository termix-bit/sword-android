package com.sword.security.android.core.model

enum class SeverityLevel(val label: String, val priority: Int) {
    CRITICAL("Critical", 5),
    HIGH("High", 4),
    MEDIUM("Medium", 3),
    LOW("Low", 2),
    INFO("Info", 1)
}
