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

package de.r4md4c.gamedealz.network

import android.os.Build
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import de.r4md4c.gamedealz.network.client.Tls12SocketFactory
import de.r4md4c.gamedealz.network.json.ApplicationJsonAdapterFactory
import de.r4md4c.gamedealz.network.repository.*
import de.r4md4c.gamedealz.network.scrapper.JsoupScrapper
import de.r4md4c.gamedealz.network.scrapper.Scrapper
import de.r4md4c.gamedealz.network.service.IsThereAnyDealScrappingService
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.SearchService
import de.r4md4c.gamedealz.network.service.steam.SteamService
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import javax.net.ssl.SSLContext


val NETWORK = module {

    single<IsThereAnyDealService> {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .client(okHttpClient.newBuilder().cache(get()).build())
            .baseUrl("https://api.isthereanydeal.com/")
            .addCallAdapterFactory(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(IsThereAnyDealService::class.java)
    }

    single<SteamService> {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .client(okHttpClient.newBuilder().cache(get()).build())
            .baseUrl("https://store.steampowered.com/api/")
            .addCallAdapterFactory(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(SteamService::class.java)
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .enableTls12OnPreLollipop()
            .build()
    }

    factory {
        val okHttpCacheDir = File(androidContext().cacheDir, "http-cache")
        Cache(okHttpCacheDir, 50 * 1024 * 1024) // 50 MB
    }

    single {
        HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    single<CallAdapter.Factory> {
        CoroutineCallAdapterFactory()
    }

    single {
        Moshi.Builder()
            .add(ApplicationJsonAdapterFactory.INSTANCE)
            .build()
    }

    factory { IsThereAnyDealRepository(get()) }

    factory<PlainsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<PricesRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<RegionsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<StoresRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<DealsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<SearchService> { IsThereAnyDealScrappingService(get(), get()) }

    factory<SteamRemoteRepository> { SteamRepository(get()) }

    factory<Scrapper> { JsoupScrapper(get()) }

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
        } catch (exc: Exception) {
            Timber.e(exc, "Error while setting TLS 1.2")
        }
    }

    return this
}