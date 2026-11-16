package com.sword.security.android

import com.sword.security.android.domain.tools.CryptoToolsExecutor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoToolsExecutorTest {

    private val executor = CryptoToolsExecutor()

    @Test
    fun `generateHashes computes known SHA-256 for abc`() = runTest {
        val payload = executor.generateHashes(mapOf("text" to "abc")) {}
        assertTrue(
            payload.rawOutput.contains(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
            )
        )
    }

    @Test
    fun `verifyHash detects match and mismatch accurately`() = runTest {
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        val matchPayload = executor.verifyHash(
            mapOf("text" to "abc", "expected_hash" to expected, "algorithm" to "SHA-256")
        ) {}
        assertTrue(matchPayload.rawOutput.contains("MATCH VERIFIED"))

        val badPayload = executor.verifyHash(
            mapOf("text" to "abcd", "expected_hash" to expected, "algorithm" to "SHA-256")
        ) {}
        assertTrue(badPayload.rawOutput.contains("MISMATCH"))
        assertTrue(badPayload.findings.any { it.severity == "High" })
    }

    @Test
    fun `encodeDecode performs hex and rot13 accurately`() = runTest {
        val hex = executor.encodeDecode(mapOf("text" to "Sword", "mode" to "Hex Encode")) {}
        assertTrue(hex.rawOutput.contains("53776f7264"))

        val rot = executor.encodeDecode(mapOf("text" to "Hello", "mode" to "ROT13")) {}
        assertTrue(rot.rawOutput.contains("Uryyb"))
    }

    @Test
    fun `generateSecureRandom produces expected byte length`() = runTest {
        val res = executor.generateSecureRandom(mapOf("bytes" to "16")) {}
        assertTrue(res.rawOutput.contains("128 bits"))
        assertTrue(res.rawOutput.contains("UUID v4"))
    }
}
