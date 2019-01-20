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

package de.r4md4c.gamedealz.domain

import de.r4md4c.commonproviders.COMMON_PROVIDERS
import de.r4md4c.gamedealz.data.DATA
import de.r4md4c.gamedealz.domain.usecase.*
import de.r4md4c.gamedealz.domain.usecase.impl.*
import de.r4md4c.gamedealz.domain.usecase.impl.internal.PickMinimalWatcheesPricesHelper
import de.r4md4c.gamedealz.domain.usecase.impl.internal.PriceAlertsHelper
import de.r4md4c.gamedealz.domain.usecase.impl.internal.RetrievePricesGroupedByCountriesHelper
import de.r4md4c.gamedealz.network.NETWORK
import org.koin.dsl.module.module

val DOMAIN = listOf(DATA, NETWORK, COMMON_PROVIDERS, module {

    factory<GetRegionsUseCase> { GetRegionsUseCaseImpl(get(), get(), get(), get()) }

    factory<GetCurrentActiveRegionUseCase> { GetCurrentActiveRegionUseCaseImpl(get(), get(), get()) }

    factory<OnCurrentActiveRegionReactiveUseCase> { GetCurrentActiveRegionUseCaseImpl(get(), get(), get()) }

    factory<ChangeActiveRegionUseCase> { ChangeActiveRegionUseCaseImpl(get(), get(), get(), get()) }

    factory<GetStoresUseCase> { GetStoresUseCaseImpl(get(), get(), get()) }

    factory<ToggleStoresUseCase> { ToggleStoresUseCaseImpl(get()) }

    factory<GetDealsUseCase> { GetDealsUseCaseImpl(get(), get(), get(), get(), get()) }

    factory<GetSelectedStoresUseCase> { GetSelectedStoresUseCaseImpl(get()) }

    factory<GetImageUrlUseCase> { GetImageUrlFromSteamUseCaseImpl(get(), get()) }

    factory<GetCountriesUnderRegionUseCase> { GetCountriesUnderRegionUseCaseImpl(get()) }

    factory<GetPlainDetails> { GetPlainDetailsImpl(get(), get(), get(), get(), get()) }

    factory<SearchUseCase> { SearchUseCaseImpl(get(), get(), get(), get(), get()) }

    factory<AddToWatchListUseCase> { AddToWatchListUseCaseImpl(get(), get(), get(), get(), get(), get()) }

    factory<IsGameAddedToWatchListUseCase> { IsGameAddedToWatchListUseCaseImpl(get()) }

    factory<CheckPriceThresholdUseCase> {
        CheckPriceThresholdUseCaseImpl(get(), get(), get(), get(), get(), get())
    }

    factory<GetLatestWatchlistCheckDate> { GetLatestWatchlistCheckDateImpl(get()) }

    factory<RemoveWatcheesUseCase> { RemoveWatcheesUseCaseImpl(get()) }

    factory { RetrievePricesGroupedByCountriesHelper(get()) }

    factory { PickMinimalWatcheesPricesHelper(get(), get(), get()) }

    factory { PriceAlertsHelper(get(), get()) }

    factory<GetAlertsCountUseCase> { GetPriceAlertsCountUseCase(get()) }

    factory<GetWatchlistToManageUseCase> { GetWatchlistToManageUseCaseImpl(get(), get(), get()) }

    factory<MarkNotificationAsReadUseCase> { MarkNotificationAsReadUseCaseImpl(get()) }

    factory<RemoveFromWatchlistUseCase> { RemoveFromWatchlistUseCaseImpl(get()) }

    factory<GetHighlightsUseCase> { GetHighlightsUseCaseImpl(get(), get(), get(), get(), get(), get()) }

})
