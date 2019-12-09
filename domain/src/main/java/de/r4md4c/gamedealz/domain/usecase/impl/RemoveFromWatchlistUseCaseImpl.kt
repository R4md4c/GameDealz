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

import de.r4md4c.gamedealz.data.repository.WatchlistRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import javax.inject.Inject

internal class RemoveFromWatchlistUseCaseImpl @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : RemoveFromWatchlistUseCase {

    override suspend fun invoke(param: TypeParameter<String>?): Boolean {
        val plainId = requireNotNull(param).value

        return watchlistRepository.removeById(plainId) > 0
    }
}
