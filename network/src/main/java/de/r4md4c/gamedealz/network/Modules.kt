package de.r4md4c.gamedealz.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.r4md4c.gamedealz.network.repository.*
import de.r4md4c.gamedealz.network.scrapper.JsoupScrapper
import de.r4md4c.gamedealz.network.scrapper.Scrapper
import de.r4md4c.gamedealz.network.service.IsThereAnyDealScrappingService
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import de.r4md4c.gamedealz.network.service.SearchService
import de.r4md4c.gamedealz.network.service.steam.SteamService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val NETWORK = module {

    single<IsThereAnyDealService> {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.isthereanydeal.com/")
            .addCallAdapterFactory(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(IsThereAnyDealService::class.java)
    }

    single<SteamService> {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://store.steampowered.com/api/")
            .addCallAdapterFactory(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(SteamService::class.java)
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
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
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    factory { IsThereAnyDealRepository(get()) }

    factory<PlainsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<PricesRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<RegionsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<StoresRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<DealsRemoteRepository> { get<IsThereAnyDealRepository>() }

    factory<SearchService> { IsThereAnyDealScrappingService(get()) }

    factory<SteamRemoteRepository> { SteamRepository(get()) }

    factory<Scrapper> { JsoupScrapper(get()) }

}