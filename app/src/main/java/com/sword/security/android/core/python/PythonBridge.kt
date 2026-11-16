package com.sword.security.android.core.python

import android.content.Context
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PythonRuntimeStatus(
    val available: Boolean,
    val pythonVersion: String,
    val engine: String,
    val error: String? = null
)

@Serializable
data class UsernameScanResponse(
    val username: String,
    val sites_checked: Int,
    val matches_found: Int,
    val matches: List<UsernameMatch>
)

@Serializable
data class UsernameMatch(
    val site: String,
    val category: String,
    val url: String,
    val status: Int
)

class PythonBridge(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Synchronized
    fun ensureStarted(): Boolean {
        return try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context.applicationContext))
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun getRuntimeStatus(): PythonRuntimeStatus = withContext(Dispatchers.IO) {
        try {
            if (!ensureStarted()) {
                return@withContext PythonRuntimeStatus(
                    available = false,
                    pythonVersion = "Unavailable",
                    engine = "Chaquopy",
                    error = "Failed to initialize AndroidPlatform for Python"
                )
            }
            val py = Python.getInstance()
            val mod = py.getModule("sword_py")
            val info = mod.callAttr("runtime_info").asMap()
            val version = info.entries.firstOrNull { it.key.toString() == "python_version" }
                ?.value?.toString() ?: "3.10"
            PythonRuntimeStatus(
                available = true,
                pythonVersion = version,
                engine = "Chaquopy Embedded Python"
            )
        } catch (e: Throwable) {
            PythonRuntimeStatus(
                available = false,
                pythonVersion = "Error",
                engine = "Chaquopy",
                error = e.message ?: "Unknown Python initialization error"
            )
        }
    }

    suspend fun runUsernameScan(username: String, maxSites: Int = 25, timeoutSeconds: Double = 5.0): Result<UsernameScanResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                check(ensureStarted()) { "Embedded Python runtime is not initialized" }
                val py = Python.getInstance()
                val mod = py.getModule("sword_py.username_engine")
                val rawJson = mod.callAttr("scan_username_json", username, maxSites, timeoutSeconds).toString()
                json.decodeFromString<UsernameScanResponse>(rawJson)
            }
        }

    suspend fun computePythonHashes(text: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            check(ensureStarted()) { "Embedded Python runtime is not initialized" }
            val py = Python.getInstance()
            val mod = py.getModule("sword_py.crypto_utils")
            mod.callAttr("compute_hashes", text).toString()
        }
    }

    suspend fun validateWithPython(validatorName: String, input: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                check(ensureStarted()) { "Embedded Python runtime is not initialized" }
                val py = Python.getInstance()
                val mod = py.getModule("sword_py.validators")
                mod.callAttr(validatorName, input).toString()
            }.recoverCatching { throwable ->
                if (throwable is PyException) {
                    throw IllegalArgumentException(throwable.message ?: "Python validation failed")
                }
                throw throwable
            }
        }
}
