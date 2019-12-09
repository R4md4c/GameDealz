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

import de.r4md4c.gamedealz.data.repository.PriceAlertRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.WatcheeModel
import de.r4md4c.gamedealz.domain.usecase.MarkNotificationAsReadUseCase
import javax.inject.Inject

internal class MarkNotificationAsReadUseCaseImpl @Inject constructor(
    private val priceAlertRepository: PriceAlertRepository
) : MarkNotificationAsReadUseCase {

    override suspend fun invoke(param: TypeParameter<WatcheeModel>?) {
        val watcheeModelId = requireNotNull(param).value.id ?: return

        priceAlertRepository.removeByWatcheeId(watcheeModelId)
    }
}
