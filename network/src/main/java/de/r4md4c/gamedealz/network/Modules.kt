package de.r4md4c.gamedealz.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.r4md4c.gamedealz.network.repository.IsThereAnyDealRepository
import de.r4md4c.gamedealz.network.repository.RegionsRepository
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import okhttp3.OkHttpClient
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val NETWORK = module {

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.isthereanydeal.com/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(IsThereAnyDealService::class.java)
    }

    single {
        OkHttpClient.Builder()
            .build()
    }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single { IsThereAnyDealRepository(get()) }

    single<RegionsRepository> { get<IsThereAnyDealRepository>() }

}