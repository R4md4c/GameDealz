package de.r4md4c.commonproviders

import de.r4md4c.commonproviders.configuration.AndroidConfiguration
import de.r4md4c.commonproviders.configuration.AndroidConfigurationImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val COMMON_PROVIDERS = module {

    factory<AndroidConfiguration> { AndroidConfigurationImpl(androidContext()) }

}