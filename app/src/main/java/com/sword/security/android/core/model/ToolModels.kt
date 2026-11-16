package com.sword.security.android.core.model

import kotlinx.serialization.Serializable

enum class ParameterType {
    TEXT,
    URL,
    DOMAIN,
    IP_ADDRESS,
    SUBNET,
    PORT_LIST,
    USERNAME,
    INTEGER,
    SELECT,
    FILE_URI
}

data class ToolParameter(
    val key: String,
    val label: String,
    val hint: String,
    val type: ParameterType,
    val required: Boolean = true,
    val defaultValue: String = "",
    val options: List<String> = emptyList()
)

enum class SwordCompatibility {
    NATIVE_ANDROID,
    REUSED_PYTHON,
    SUBSTANTIALLY_MODIFIED,
    UNSUPPORTED_ON_ANDROID
}

data class ToolDescriptor(
    val id: String,
    val name: String,
    val category: ModuleCategory,
    val summary: String,
    val description: String,
    val compatibility: SwordCompatibility,
    val compatibilityNotes: String,
    val requiredPermissions: List<String> = emptyList(),
    val requiresNetwork: Boolean = false,
    val requiresAuthorizationConfirmation: Boolean = false,
    val parameters: List<ToolParameter> = emptyList(),
    val isExecutable: Boolean = true,
    val staticLimitationReason: String? = null
)

@Serializable
data class SecurityFinding(
    val title: String,
    val severity: String,
    val detail: String,
    val recommendation: String
)

enum class ExecutionStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class ExecutionErrorType {
    NONE,
    VALIDATION_ERROR,
    PERMISSION_DENIED,
    CAPABILITY_UNAVAILABLE,
    NETWORK_ERROR,
    TIMEOUT,
    PYTHON_RUNTIME_ERROR,
    CANCELLED,
    INTERNAL_ERROR
}

data class ExecutionProgress(
    val taskId: String = "",
    val toolId: String = "",
    val toolName: String = "",
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val progressFraction: Float = 0f,
    val stageMessage: String = "",
    val liveLogs: List<String> = emptyList(),
    val result: ExecutionResult? = null
)

data class ExecutionResult(
    val taskId: String,
    val toolId: String,
    val toolName: String,
    val category: ModuleCategory,
    val targetSummary: String,
    val success: Boolean,
    val durationMs: Long,
    val timestampEpochMs: Long,
    val rawOutput: String,
    val findings: List<SecurityFinding>,
    val highestSeverity: SeverityLevel,
    val errorType: ExecutionErrorType = ExecutionErrorType.NONE,
    val errorMessage: String? = null,
    val reportId: Long? = null
)
