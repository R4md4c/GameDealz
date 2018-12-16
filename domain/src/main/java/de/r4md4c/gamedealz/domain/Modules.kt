package de.r4md4c.gamedealz.domain

import de.r4md4c.commonproviders.COMMON_PROVIDERS
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.GetCurrentActiveRegionUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetRegionsUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetStoresUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.StoresSelectionUseCaseImpl
import de.r4md4c.gamedealz.network.NETWORK
import org.koin.dsl.module.module

val DOMAIN = listOf(DATA, NETWORK, COMMON_PROVIDERS, module {

    factory<GetRegionsUseCase> { GetRegionsUseCaseImpl(get(), get()) }

    factory<GetCurrentActiveRegionUseCase> { GetCurrentActiveRegionUseCaseImpl(get(), get(), get()) }

    factory<GetStoresUseCase> { GetStoresUseCaseImpl(get(), get(), get()) }

    factory<ToggleStoresUseCase> { StoresSelectionUseCaseImpl(get()) }
})
