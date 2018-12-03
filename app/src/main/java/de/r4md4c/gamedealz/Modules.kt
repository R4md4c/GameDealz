package de.r4md4c.gamedealz

import okhttp3.OkHttpClient
import org.koin.dsl.module.module

val MAIN = module {

    single {
        OkHttpClient.Builder()
            .build()
    }
}