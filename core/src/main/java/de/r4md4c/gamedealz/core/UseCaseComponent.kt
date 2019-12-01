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

package de.r4md4c.gamedealz.core

import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.domain.usecase.OnNightModeChangeUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase

interface UseCaseComponent {

    val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase

    val getOnCurrentActiveRegionUseCase: OnCurrentActiveRegionReactiveUseCase

    val getStoresUseCase: GetStoresUseCase

    val toggleStoresUseCase: ToggleStoresUseCase

    val getAlertsCountUseCase: GetAlertsCountUseCase

    val toggleNightModeChangeUseCase: ToggleNightModeUseCase

    val onNightModeChangeUseCase: OnNightModeChangeUseCase

    val getSelectedStoresUseCase: GetSelectedStoresUseCase

    val getDealsUseCase: GetDealsUseCase

    val getPlainDealsUseCase: GetPlainDetails

    val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase

    val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase

    val searchUseCase: SearchUseCase
}
