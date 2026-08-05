package com.sotospeak.app.di

import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import androidx.compose.runtime.Composable

@Composable
fun InitializeCoil() {
    val httpClient = HttpClient(OkHttp)
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .crossfade(true)
            .build()
    }
}
