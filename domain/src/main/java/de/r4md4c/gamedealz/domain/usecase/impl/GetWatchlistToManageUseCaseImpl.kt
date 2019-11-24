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

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.entity.Watchee
import de.r4md4c.gamedealz.data.repository.PriceAlertRepository
import de.r4md4c.gamedealz.data.repository.RegionsRepository
import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.domain.model.toCurrencyModel
import de.r4md4c.gamedealz.domain.model.toModel
import de.r4md4c.gamedealz.domain.usecase.GetWatchlistToManageUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.mapNotNull

internal class GetWatchlistToManageUseCaseImpl(
    private val watchlistRepository: WatchlistRepository,
    private val regionsRepository: RegionsRepository,
    private val priceAlertRepository: PriceAlertRepository
) : GetWatchlistToManageUseCase {

    override suspend fun invoke(param: VoidParameter?): Flow<List<ManageWatchlistModel>> = coroutineScope {
        watchlistRepository.all().map { it.toManageWatchlistModel() }
    }

    private suspend fun List<Watchee>.toManageWatchlistModel(): List<ManageWatchlistModel> = mapNotNull {
        val region = regionsRepository.findById(it.regionCode) ?: return@mapNotNull null
        val hasNotification = priceAlertRepository.findByWatcheeId(it.id) != null
        ManageWatchlistModel(it.toModel(), hasNotification, region.currency.toCurrencyModel())
    }
}
