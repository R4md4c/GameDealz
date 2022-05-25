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

package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.data.repository.WatchlistLocalDataSource
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveFromWatchlistUseCase @Inject constructor(
    private val watchlistRepository: WatchlistLocalDataSource,
    private val dispatchers: IDispatchers
) {

    suspend fun invoke(plainId: String): Boolean {
        return withContext(dispatchers.IO) {
            watchlistRepository.removeById(plainId) > 0
        }
    }
}
