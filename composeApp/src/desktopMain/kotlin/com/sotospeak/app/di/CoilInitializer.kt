package com.sotospeak.app.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import okhttp3.ConnectionSpec
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okio.Path.Companion.toOkioPath
import java.io.File
import androidx.compose.runtime.Composable
import io.github.aakira.napier.Napier

@Composable
fun InitializeCoil() {
    Napier.d("Initializing Coil with KtorNetworkFetcherFactory...", tag = "CoilInit")
    
    // Create OkHttpClient with HTTP support and SSL bypass for development
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    
    val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }
    
    val okHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
        .build()
    
    // Create Ktor HttpClient with OkHttp engine
    val ktorClient = HttpClient(OkHttp) {
        engine {
            preconfigured = okHttpClient
        }
    }
    
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(ktorClient))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(System.getProperty("java.io.tmpdir"), "coil_cache").toOkioPath())
                    .maxSizeBytes(256L * 1024 * 1024) // 256MB
                    .build()
            }
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}
