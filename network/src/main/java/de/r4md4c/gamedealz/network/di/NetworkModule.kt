/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.network.di

import android.content.Context
import android.os.Build
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import de.r4md4c.gamedealz.network.BuildConfig
import de.r4md4c.gamedealz.network.MB_50
import de.r4md4c.gamedealz.network.READ_TIMEOUT_IN_SECONDS
import de.r4md4c.gamedealz.network.client.Tls12SocketFactory
import de.r4md4c.gamedealz.network.json.ApplicationJsonAdapterFactory
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.steam.SteamService
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext

@Module(includes = [NetworkBindsModule::class])
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .enableTls12OnPreLollipop()
            .build()

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)

    @Provides
    fun provideCallAdapterFactory(): CallAdapter.Factory = CoroutineCallAdapterFactory()

    @Singleton
    @Provides
    fun provideMoshiInstance(): Moshi =
        Moshi.Builder()
            .add(ApplicationJsonAdapterFactory.INSTANCE)
            .build()

    @Singleton
    @Provides
    fun provideCache(context: Context): Cache {
        val okHttpCacheDir = File(context.cacheDir, "http-cache")
        return Cache(okHttpCacheDir, MB_50)
    }

    @Singleton
    @Provides
    internal fun provideIsThereAnyDealService(
        httpClient: Lazy<OkHttpClient>,
        callAdapterFactory: CallAdapter.Factory,
        moshi: Moshi
    ): IsThereAnyDealService {
        return Retrofit.Builder()
            .callFactory { httpClient.get().newCall(it) }
            .baseUrl("https://api.isthereanydeal.com/")
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(IsThereAnyDealService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideSteamService(
        httpClient: Lazy<OkHttpClient>,
        callAdapterFactory: CallAdapter.Factory,
        moshi: Moshi
    ): SteamService =
        Retrofit.Builder()
            .callFactory { httpClient.get().newCall(it) }
            .baseUrl("https://store.steampowered.com/api/")
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SteamService::class.java)

}

private fun OkHttpClient.Builder.enableTls12OnPreLollipop(): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.JELLY_BEAN..Build.VERSION_CODES.KITKAT) {
        try {
            val sc = SSLContext.getInstance("TLSv1.2")
            sc.init(null, null, null)
            sslSocketFactory(Tls12SocketFactory(sc.socketFactory))

            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2).build()

            val specs = arrayListOf(cs, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)

            connectionSpecs(specs)
        } catch (exc: NoSuchAlgorithmException) {
            Timber.e(exc, "Error while setting TLS 1.2")
        }
    }

    return this
}
