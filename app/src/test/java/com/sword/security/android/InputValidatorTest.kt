package com.sword.security.android

import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.core.validation.ValidationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun `valid IPv4 and IPv6 addresses pass`() {
        assertEquals("192.168.1.1", InputValidator.validateIpAddress(" 192.168.1.1 "))
        assertEquals("2001:4860:4860::8888", InputValidator.validateIpAddress("2001:4860:4860::8888"))
    }

    @Test
    fun `invalid IP addresses throw ValidationException`() {
        assertThrows(ValidationException::class.java) {
            InputValidator.validateIpAddress("999.168.1.1")
        }
        assertThrows(ValidationException::class.java) {
            InputValidator.validateIpAddress("192.168.1.1; rm -rf /")
        }
    }

    @Test
    fun `subnet validation and expansion stay bounded`() {
        assertEquals("192.168.10.0/24", InputValidator.validateIpv4Subnet("192.168.10.0/24"))
        val hosts = InputValidator.expandIpv4SubnetHosts("192.168.10.0/29")
        assertEquals(6, hosts.size)
        assertEquals("192.168.10.1", hosts.first())
        assertEquals("192.168.10.6", hosts.last())

        assertThrows(ValidationException::class.java) {
            InputValidator.validateIpv4Subnet("10.0.0.0/8")
        }
    }

    @Test
    fun `port list parses individual ports and small ranges`() {
        val ports = InputValidator.validatePortList("22, 80, 443, 8000-8002")
        assertEquals(listOf(22, 80, 443, 8000, 8001, 8002), ports)

        assertThrows(ValidationException::class.java) {
            InputValidator.validatePortList("1-500")
        }
    }

    @Test
    fun `url validator blocks embedded credentials and non-http schemes`() {
        assertEquals("https://example.com/test", InputValidator.validateHttpUrl("example.com/test"))

        assertThrows(ValidationException::class.java) {
            InputValidator.validateHttpUrl("https://user:pass@example.com")
        }
        assertThrows(ValidationException::class.java) {
            InputValidator.validateHttpUrl("file:///etc/passwd")
        }
    }

    @Test
    fun `domain and username validators enforce strict character sets`() {
        assertEquals("sub.example.com", InputValidator.validateDomain("SUB.Example.COM."))
        assertEquals("octocat_1", InputValidator.validateUsername("octocat_1"))

        assertThrows(ValidationException::class.java) {
            InputValidator.validateDomain("bad domain!.com")
        }
        assertThrows(ValidationException::class.java) {
            InputValidator.validateUsername("bad/user")
        }
    }

    @Test
    fun `proxy validator accepts valid http or socks5 and rejects invalid`() {
        assertEquals("", InputValidator.validateProxyUrl(""))
        assertEquals("http://127.0.0.1:8080", InputValidator.validateProxyUrl("http://127.0.0.1:8080"))
        assertTrue(InputValidator.validateProxyUrl("socks5://127.0.0.1:1080").startsWith("socks5://"))

        assertThrows(ValidationException::class.java) {
            InputValidator.validateProxyUrl("ftp://127.0.0.1:21")
        }
    }
}
