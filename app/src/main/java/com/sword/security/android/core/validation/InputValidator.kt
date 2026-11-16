package com.sword.security.android.core.validation

import java.net.InetAddress
import java.net.URI

class ValidationException(message: String) : IllegalArgumentException(message)

object InputValidator {

    private val IPV4_REGEX = Regex(
        "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
    )
    private val DOMAIN_LABEL_REGEX = Regex("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9._-]{1,50}$")
    private val HEX_REGEX = Regex("^[0-9a-fA-F]+$")

    fun validateIpAddress(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty() || value.length > 45) {
            throw ValidationException("Invalid IP address length")
        }
        if (IPV4_REGEX.matches(value)) {
            return value
        }
        // Validate IPv6 without performing DNS lookup
        if (value.contains(":")) {
            val validV6 = try {
                val addr = InetAddress.getByName(value)
                addr.hostAddress != null && value.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == ':' || it == '.' }
            } catch (_: Exception) {
                false
            }
            if (validV6) return value
        }
        throw ValidationException("Invalid IPv4 or IPv6 address: '$value'")
    }

    fun validateIpv4Subnet(raw: String, minPrefix: Int = 22, maxPrefix: Int = 30): String {
        val value = raw.trim()
        val parts = value.split("/")
        if (parts.size != 2) {
            throw ValidationException("Subnet must use CIDR notation (e.g. 192.168.1.0/24)")
        }
        val ip = parts[0]
        if (!IPV4_REGEX.matches(ip)) {
            throw ValidationException("Invalid IPv4 base address in subnet")
        }
        val prefix = parts[1].toIntOrNull()
            ?: throw ValidationException("Invalid CIDR prefix")
        if (prefix !in minPrefix..maxPrefix) {
            throw ValidationException("CIDR prefix must be between /$minPrefix and /$maxPrefix for safe mobile scanning")
        }
        return "$ip/$prefix"
    }

    fun expandIpv4SubnetHosts(cidr: String, maxHosts: Int = 254): List<String> {
        val valid = validateIpv4Subnet(cidr)
        val parts = valid.split("/")
        val octets = parts[0].split(".").map { it.toInt() }
        val prefix = parts[1].toInt()
        val ipInt = (octets[0] shl 24) or (octets[1] shl 16) or (octets[2] shl 8) or octets[3]
        val mask = if (prefix == 0) 0 else (-1 shl (32 - prefix))
        val network = ipInt and mask
        val broadcast = network or mask.inv()
        val hosts = mutableListOf<String>()
        var current = network + 1
        while (current < broadcast && hosts.size < maxHosts) {
            hosts += "${(current ushr 24) and 0xFF}.${(current ushr 16) and 0xFF}.${(current ushr 8) and 0xFF}.${current and 0xFF}"
            current++
        }
        return hosts
    }

    fun validatePort(raw: String): Int {
        val value = raw.trim()
        val port = value.toIntOrNull() ?: throw ValidationException("Port must be an integer")
        if (port !in 1..65535) {
            throw ValidationException("Port must be between 1 and 65535")
        }
        return port
    }

    fun validatePortList(raw: String, maxPorts: Int = 64): List<Int> {
        val value = raw.trim()
        if (value.isEmpty()) {
            throw ValidationException("Port list cannot be empty")
        }
        val result = linkedSetOf<Int>()
        for (token in value.split(",")) {
            val part = token.trim()
            if (part.isEmpty()) continue
            if ("-" in part) {
                val bounds = part.split("-")
                if (bounds.size != 2) throw ValidationException("Invalid port range: '$part'")
                val start = validatePort(bounds[0])
                val end = validatePort(bounds[1])
                if (start > end) throw ValidationException("Invalid port range order: '$part'")
                if (end - start + 1 > maxPorts) {
                    throw ValidationException("Port range exceeds maximum of $maxPorts ports")
                }
                for (p in start..end) {
                    result += p
                    if (result.size > maxPorts) {
                        throw ValidationException("Total ports exceed maximum of $maxPorts")
                    }
                }
            } else {
                result += validatePort(part)
                if (result.size > maxPorts) {
                    throw ValidationException("Total ports exceed maximum of $maxPorts")
                }
            }
        }
        if (result.isEmpty()) {
            throw ValidationException("No valid ports specified")
        }
        return result.toList()
    }

    fun validateDomain(raw: String): String {
        val value = raw.trim().lowercase().trimEnd('.')
        if (value.isEmpty() || value.length > 253 || !value.contains('.')) {
            throw ValidationException("Invalid domain name")
        }
        val labels = value.split('.')
        for (label in labels) {
            if (!DOMAIN_LABEL_REGEX.matches(label)) {
                throw ValidationException("Invalid domain label: '$label'")
            }
        }
        return value
    }

    fun validateHostOrIp(raw: String): String {
        val value = raw.trim()
        return try {
            validateIpAddress(value)
        } catch (_: ValidationException) {
            validateDomain(value)
        }
    }

    fun validateHttpUrl(raw: String, requireHttps: Boolean = false): String {
        var value = raw.trim()
        if (value.isEmpty() || value.length > 2048) {
            throw ValidationException("URL is empty or exceeds max length")
        }
        if (value.any { it.isWhitespace() || it == '\u0000' }) {
            throw ValidationException("URL contains invalid whitespace or control characters")
        }
        if (!value.contains("://")) {
            value = "https://$value"
        }
        val uri = try {
            URI(value)
        } catch (e: Exception) {
            throw ValidationException("Malformed URL: ${e.message}")
        }
        val scheme = uri.scheme?.lowercase()
        if (requireHttps && scheme != "https") {
            throw ValidationException("HTTPS scheme is required")
        }
        if (scheme != "http" && scheme != "https") {
            throw ValidationException("Only http and https URLs are permitted")
        }
        if (!uri.userInfo.isNullOrEmpty()) {
            throw ValidationException("Embedded URL credentials are not permitted")
        }
        val host = uri.host ?: throw ValidationException("URL must include a valid host")
        validateHostOrIp(host)
        if (uri.port != -1 && uri.port !in 1..65535) {
            throw ValidationException("URL port out of range")
        }
        return uri.toASCIIString()
    }

    fun validateUsername(raw: String): String {
        val value = raw.trim()
        if (!USERNAME_REGEX.matches(value)) {
            throw ValidationException("Username must be 1-50 chars (letters, numbers, ., _, -)")
        }
        return value
    }

    fun validateHexString(raw: String): String {
        val value = raw.trim().replace(" ", "")
        if (value.isEmpty() || value.length % 2 != 0 || !HEX_REGEX.matches(value)) {
            throw ValidationException("Invalid hexadecimal string")
        }
        return value
    }

    fun validateIntegerInRange(raw: String, min: Int, max: Int, fieldName: String = "Value"): Int {
        val parsed = raw.trim().toIntOrNull()
            ?: throw ValidationException("$fieldName must be an integer")
        if (parsed !in min..max) {
            throw ValidationException("$fieldName must be between $min and $max")
        }
        return parsed
    }

    fun validateProxyUrl(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty()) return ""
        val uri = try {
            URI(value)
        } catch (e: Exception) {
            throw ValidationException("Invalid proxy URL")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme !in listOf("http", "socks", "socks5")) {
            throw ValidationException("Proxy scheme must be http or socks5")
        }
        val host = uri.host ?: throw ValidationException("Proxy URL must specify a host")
        validateHostOrIp(host)
        if (uri.port !in 1..65535) {
            throw ValidationException("Proxy URL must specify a valid port (1-65535)")
        }
        return value
    }
}
