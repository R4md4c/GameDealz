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

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.DealsResult
import de.r4md4c.gamedealz.domain.model.toDealModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.network.repository.DealsRemoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class GetDealsUseCaseImpl @Inject constructor(
    private val dealsRemoteRepository: DealsRemoteRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val selectedStoresUseCase: GetSelectedStoresUseCase,
    private val getImageUrlUseCase: GetImageUrlUseCase,
    private val storesRepository: StoresLocalDataSource
) : GetDealsUseCase {

    override suspend fun invoke(param: PageParameter?): DealsResult {
        requireNotNull(param)

        return withContext(IO) {
            val activeRegion = activeRegionUseCase()
            dealsRemoteRepository.deals(param.offset,
                param.pageSize,
                activeRegion.regionCode,
                activeRegion.country.code,
                selectedStoresUseCase().first().map { it.id }.toSet()
            )
                .run {
                    this.totalCount to page.mapNotNull {
                        val shopColor = storesRepository.findById(it.shop.id)?.color ?: return@mapNotNull null
                        it.toDealModel(activeRegion.currency, shopColor).loadWithImage()
                    }
                }
        }
    }

    private suspend fun DealModel.loadWithImage() =
        copy(urls = urls.copy(imageUrl = getImageUrlUseCase(TypeParameter(gameId))))
}
