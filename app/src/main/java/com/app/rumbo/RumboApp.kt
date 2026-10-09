package com.app.rumbo

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class RumboApp : Application(), SingletonImageLoader.Factory {

    /**
     * Cargador de imágenes de Coil para toda la app. Wikimedia rechaza las descargas
     * sin un User-Agent que identifique la app, por eso se agrega a cada petición.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", USER_AGENT).build())
            }
            .build()
        return ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { client })) }
            .crossfade(true)
            .build()
    }

    private companion object {
        const val USER_AGENT = "Rumbo/1.0 (Android; +https://github.com/AndreaYr/App-Movil-Rumbo)"
    }
}
