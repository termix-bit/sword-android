package com.sword.security.android.core.network

import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    fun create(
        timeoutSeconds: Int = 10,
        proxyUrl: String = "",
        followRedirects: Boolean = true
    ): OkHttpClient {
        val clampedTimeout = timeoutSeconds.coerceIn(3, 60).toLong()
        val builder = OkHttpClient.Builder()
            .connectTimeout(clampedTimeout, TimeUnit.SECONDS)
            .readTimeout(clampedTimeout, TimeUnit.SECONDS)
            .writeTimeout(clampedTimeout, TimeUnit.SECONDS)
            .callTimeout(clampedTimeout * 2, TimeUnit.SECONDS)
            .followRedirects(followRedirects)
            .followSslRedirects(followRedirects)

        val trimmedProxy = proxyUrl.trim()
        if (trimmedProxy.isNotEmpty()) {
            parseProxy(trimmedProxy)?.let { builder.proxy(it) }
        }

        return builder.build()
    }

    private fun parseProxy(proxyUrl: String): Proxy? {
        return try {
            val uri = URI(proxyUrl)
            val host = uri.host ?: return null
            val port = uri.port.takeIf { it in 1..65535 } ?: return null
            val type = when (uri.scheme?.lowercase()) {
                "socks", "socks5" -> Proxy.Type.SOCKS
                "http", "https" -> Proxy.Type.HTTP
                else -> return null
            }
            Proxy(type, InetSocketAddress(host, port))
        } catch (_: Exception) {
            null
        }
    }
}
