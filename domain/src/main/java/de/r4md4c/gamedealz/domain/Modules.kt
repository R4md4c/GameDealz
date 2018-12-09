package de.r4md4c.gamedealz.domain

import de.r4md4c.commonproviders.COMMON_PROVIDERS
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegion
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionImpl
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoredRegionsUseCase
import de.r4md4c.gamedealz.network.NETWORK
import org.koin.dsl.module.module

val DOMAIN = listOf(DATA, NETWORK, COMMON_PROVIDERS, module {

    factory<GetRegionsUseCase> { GetStoredRegionsUseCase(get(), get()) }

    factory<GetCurrentActiveRegion> { GetCurrentActiveRegionImpl(get(), get()) }
})