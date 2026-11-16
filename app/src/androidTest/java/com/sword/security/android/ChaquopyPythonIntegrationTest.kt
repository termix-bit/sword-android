package com.sword.security.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.sword.security.android.core.python.PythonBridge
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChaquopyPythonIntegrationTest {

    @Test
    fun chaquopyInitializesAndExecutesSwordPyFunctionsOnAndroid() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val bridge = PythonBridge(context)

        // 1. Initialize Python through Android
        assertTrue("Chaquopy AndroidPlatform must initialize", bridge.ensureStarted())
        assertTrue(Python.isStarted())

        // Verify direct Chaquopy Python.getInstance() import of sword_py
        val py = Python.getInstance()
        val swordPyModule = py.getModule("sword_py")
        val infoMap = swordPyModule.callAttr("runtime_info").asMap()
        val pyVersion = infoMap.entries
            .firstOrNull { it.key.toString() == "python_version" }
            ?.value?.toString()
            .orEmpty()
        assertTrue("Expected Python 3.x runtime, got '$pyVersion'", pyVersion.startsWith("3."))

        // 2. Verify PythonBridge runtime status check
        val status = bridge.getRuntimeStatus()
        assertTrue(status.available)
        assertTrue(status.pythonVersion.startsWith("3."))

        // 3. Call Python SHA-256 generator from Kotlin and return result
        val hashJsonRaw = bridge.computePythonHashes("sword-android").getOrThrow()
        val hashJson = Json.parseToJsonElement(hashJsonRaw).jsonObject
        val sha256 = hashJson["sha256"]?.jsonPrimitive?.content.orEmpty()
        assertEquals(
            "b5ded66f121532eb4bb3f9df6a46427da3b878b8e8f3314368ac8430bdc86ce8",
            sha256
        )

        // 4. Call Python domain validator from Kotlin and verify return value & error handling
        val normalizedDomain = bridge.validateWithPython("validate_domain", "Example.COM").getOrThrow()
        assertEquals("example.com", normalizedDomain)

        val invalidResult = bridge.validateWithPython("validate_domain", "not a valid domain!!")
        assertTrue("Invalid domain should fail safely", invalidResult.isFailure)
    }
}
