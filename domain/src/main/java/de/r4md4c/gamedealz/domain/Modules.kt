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
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCountriesUnderRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase
import de.r4md4c.gamedealz.domain.usecase.GetLatestWatchlistCheckDate
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.GetWatchlistToManageUseCase
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveWatcheesUseCase
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.AddToWatchListUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.ChangeActiveRegionUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.CheckPriceThresholdUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetCountriesUnderRegionUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetCurrentActiveRegionUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetDealsUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetImageUrlFromSteamUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetLatestWatchlistCheckDateImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetPlainDetailsImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetPriceAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.GetRegionsUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetSelectedStoresUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetStoresUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetWatchlistToManageUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.IsGameAddedToWatchListUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.MarkNotificationAsReadUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.OnNightModeChangeUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.RemoveFromWatchlistUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.RemoveWatcheesUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.SearchUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.ToggleNightModeUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.ToggleStoresUseCaseImpl
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

    factory<OnNightModeChangeUseCase> { OnNightModeChangeUseCaseImpl(get()) }

    factory<ToggleNightModeUseCase> { ToggleNightModeUseCaseImpl(get(), get()) }

    factory<GetLatestWatchlistCheckDate> { GetLatestWatchlistCheckDateImpl(get()) }

    factory<RemoveWatcheesUseCase> { RemoveWatcheesUseCaseImpl(get()) }

    factory { RetrievePricesGroupedByCountriesHelper(get()) }

    factory { PickMinimalWatcheesPricesHelper(get(), get(), get()) }

    factory { PriceAlertsHelper(get(), get()) }

    factory<GetAlertsCountUseCase> { GetPriceAlertsCountUseCase(get()) }

    factory<GetWatchlistToManageUseCase> { GetWatchlistToManageUseCaseImpl(get(), get(), get()) }

    factory<MarkNotificationAsReadUseCase> { MarkNotificationAsReadUseCaseImpl(get()) }

    factory<RemoveFromWatchlistUseCase> { RemoveFromWatchlistUseCaseImpl(get()) }
})
