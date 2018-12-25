package de.r4md4c.gamedealz.domain

import de.r4md4c.commonproviders.COMMON_PROVIDERS
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.domain.usecase.*
import de.r4md4c.gamedealz.domain.usecase.impl.*
import de.r4md4c.gamedealz.network.NETWORK
import org.koin.dsl.module.module

val DOMAIN = listOf(DATA, NETWORK, COMMON_PROVIDERS, module {

    factory<GetRegionsUseCase> { GetRegionsUseCaseImpl(get(), get(), get(), get()) }

    factory<GetCurrentActiveRegionUseCase> { GetCurrentActiveRegionUseCaseImpl(get(), get(), get()) }

    factory<OnCurrentActiveRegionReactiveUseCase> { GetCurrentActiveRegionUseCaseImpl(get(), get(), get()) }

    factory<ChangeActiveRegionUseCase> { ChangeActiveRegionUseCaseImpl(get(), get(), get(), get()) }

    factory<GetStoresUseCase> { GetStoresUseCaseImpl(get(), get(), get()) }

    factory<ToggleStoresUseCase> { StoresSelectionUseCaseImpl(get()) }

    factory<GetDealsUseCase> { GetDealsUseCaseImpl(get(), get(), get(), get(), get()) }

    factory<GetSelectedStoresUseCase> { GetSelectedStoresUseCaseImpl(get()) }

    factory<GetImageUrlUseCase> { GetImageUrlFromSteamUseCaseImpl(get(), get()) }

    factory<GetCountriesUnderRegionUseCase> { GetCountriesUnderRegionUseCaseImpl(get()) }

    factory<SearchUseCase> { SearchUseCaseImpl(get(), get(), get(), get(), get(), get()) }
})
