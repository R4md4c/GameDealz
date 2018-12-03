package de.r4md4c.gamedealz.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import de.r4md4c.gamedealz.network.service.IsThereAnyDealService
import org.koin.dsl.module.module
import retrofit2.Retrofit

val NETWORK = module {

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.isthereanydeal.com/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(IsThereAnyDealService::class.java)
    }
}