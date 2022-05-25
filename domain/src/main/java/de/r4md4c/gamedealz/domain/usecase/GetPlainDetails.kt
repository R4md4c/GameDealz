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

import com.dropbox.android.external.store4.StoreResponse
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Resource
import de.r4md4c.gamedealz.domain.repository.GameDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPlainDetails @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) {

    fun invoke(plainId: String): Flow<Resource<PlainDetailsModel>> {
        return gameDetailsRepository.findDetails(plainId, false)
            .map {
                when (val response = it) {
                    is StoreResponse.Loading -> Resource.loading(response.dataOrNull())
                    is StoreResponse.Data -> Resource.success(response.requireData())
                    is StoreResponse.Error -> Resource.error(
                        response.error.localizedMessage,
                        response.dataOrNull()
                    )
                }
            }
    }
}
