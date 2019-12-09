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
import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.usecase.GetAlertsCountUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class GetPriceAlertsCountUseCase @Inject constructor(
    private val alertsRepository: PriceAlertRepository
) : GetAlertsCountUseCase {

    override suspend fun invoke(param: VoidParameter?): Flow<Int> = alertsRepository.unreadCount()
}
