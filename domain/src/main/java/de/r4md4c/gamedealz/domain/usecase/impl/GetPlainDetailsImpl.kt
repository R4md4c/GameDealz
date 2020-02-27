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

import com.dropbox.android.external.store4.StoreResponse
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Resource
import de.r4md4c.gamedealz.domain.repository.GameDetailsRepository
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

internal class GetPlainDetailsImpl @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) : GetPlainDetails {

    override suspend fun invoke(param: TypeParameter<GetPlainDetails.Params>?): Flow<Resource<PlainDetailsModel>> {
        requireNotNull(param)

        return gameDetailsRepository.findDetails(param.value.plainId, param.value.fresh)
            .map {
                when (it) {
                    is StoreResponse.Loading -> Resource.loading(it.dataOrNull())
                    is StoreResponse.Data -> Resource.success(it.requireData())
                    is StoreResponse.Error -> Resource.error(
                        it.error.localizedMessage,
                        it.dataOrNull()
                    )
                }
            }.onEach { Timber.d("Response $it") }
    }
}
