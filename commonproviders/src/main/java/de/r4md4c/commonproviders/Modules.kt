package de.r4md4c.commonproviders

import de.r4md4c.commonproviders.configuration.AndroidConfigurationImpl
import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.commonproviders.date.JavaDateProvider
import de.r4md4c.commonproviders.preferences.AndroidSharedPreferencesProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val COMMON_PROVIDERS = module {

    factory<ConfigurationProvider> { AndroidConfigurationImpl(androidContext()) }

    factory<DateProvider> { JavaDateProvider() }

    single<SharedPreferencesProvider> { AndroidSharedPreferencesProvider(androidContext()) }
}