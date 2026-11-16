package com.sword.security.android

import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.domain.tools.WebSecurityToolsExecutor
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebSecurityExecutorTest {

    private lateinit var server: MockWebServer
    private val executor = WebSecurityToolsExecutor()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `header audit identifies missing security headers and insecure cookies`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Server", "TestServer/1.0")
                .addHeader("Set-Cookie", "sid=12345; Path=/")
                .setBody("ok")
        )

        val url = server.url("/").toString()
        val payload = executor.inspectHttpHeaders(
            params = mapOf("url" to url),
            prefs = UserPreferences()
        ) {}

        assertTrue(payload.rawOutput.contains("HTTP Status   : 200"))
        assertTrue(payload.findings.any { it.title.contains("HSTS") })
        assertTrue(payload.findings.any { it.title.contains("Content-Security-Policy") })
        assertTrue(payload.findings.any { it.title.contains("Cookie Missing Security Attributes") })
    }
}
