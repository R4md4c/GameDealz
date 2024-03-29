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

package de.r4md4c.gamedealz.domain.di

import dagger.Binds
import dagger.Module
import de.r4md4c.gamedealz.domain.repository.GameDetailsRepository
import de.r4md4c.gamedealz.domain.repository.GameDetailsRepositoryImpl
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.CheckPriceThresholdUseCase
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase
import de.r4md4c.gamedealz.domain.usecase.GetLatestWatchlistCheckDate
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.GetWatchlistToManageUseCase
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveWatcheesUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.AddToWatchListUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.CheckPriceThresholdUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetCurrentActiveRegionUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetDealsUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetImageUrlFromSteamUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetLatestWatchlistCheckDateImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetPriceAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.GetRegionsUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetStoresUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.GetWatchlistToManageUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.MarkNotificationAsReadUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.OnNightModeChangeUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.RemoveWatcheesUseCaseImpl
import de.r4md4c.gamedealz.domain.usecase.impl.ToggleStoresUseCaseImpl

@Suppress("TooManyFunctions")
@Module(includes = [MappersModule::class])
abstract class UseCaseModule {

    @Binds
    internal abstract fun bindsGameDetailsRepository(it: GameDetailsRepositoryImpl): GameDetailsRepository

    @Binds
    internal abstract fun bindsGetRegionsUseCase(it: GetRegionsUseCaseImpl): GetRegionsUseCase

    @Binds
    internal abstract fun bindsOnCurrentActiveRegionReactiveUseCase(
        it: GetCurrentActiveRegionUseCaseImpl
    ): OnCurrentActiveRegionReactiveUseCase

    @Binds
    internal abstract fun bindsGetCurrentActiveRegionUseCase(
        it: GetCurrentActiveRegionUseCaseImpl
    ): GetCurrentActiveRegionUseCase

    @Binds
    internal abstract fun bindsGetStoresUseCase(
        it: GetStoresUseCaseImpl
    ): GetStoresUseCase

    @Binds
    internal abstract fun bindsToggleStoresUseCase(
        it: ToggleStoresUseCaseImpl
    ): ToggleStoresUseCase

    @Binds
    internal abstract fun bindsGetDealsUseCase(
        it: GetDealsUseCaseImpl
    ): GetDealsUseCase

    @Binds
    internal abstract fun bindsGetImageUrlUseCase(
        it: GetImageUrlFromSteamUseCaseImpl
    ): GetImageUrlUseCase

    @Binds
    internal abstract fun bindsAddToWatchListUseCase(
        it: AddToWatchListUseCaseImpl
    ): AddToWatchListUseCase

    @Binds
    internal abstract fun bindsCheckPriceThresholdUseCaseImpl(
        it: CheckPriceThresholdUseCaseImpl
    ): CheckPriceThresholdUseCase

    @Binds
    internal abstract fun bindsOnNightModeChangeUseCase(
        it: OnNightModeChangeUseCaseImpl
    ): OnNightModeChangeUseCase

    @Binds
    internal abstract fun bindsGetLatestWatchlistCheckDate(
        it: GetLatestWatchlistCheckDateImpl
    ): GetLatestWatchlistCheckDate

    @Binds
    internal abstract fun bindsRemoveWatcheesUseCase(
        it: RemoveWatcheesUseCaseImpl
    ): RemoveWatcheesUseCase

    @Binds
    internal abstract fun bindsGetAlertsCountUseCase(
        it: GetPriceAlertsCountUseCase
    ): GetAlertsCountUseCase

    @Binds
    internal abstract fun bindsGetWatchlistToManageUseCase(
        it: GetWatchlistToManageUseCaseImpl
    ): GetWatchlistToManageUseCase

    @Binds
    internal abstract fun bindsMarkNotificationAsReadUseCase(
        it: MarkNotificationAsReadUseCaseImpl
    ): MarkNotificationAsReadUseCase
}
