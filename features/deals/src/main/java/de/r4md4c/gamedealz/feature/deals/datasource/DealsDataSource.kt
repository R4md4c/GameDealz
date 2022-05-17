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

package de.r4md4c.gamedealz.feature.deals.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.feature.deals.R
import de.r4md4c.gamedealz.feature.deals.model.DealRenderModel
import de.r4md4c.gamedealz.feature.deals.model.toRenderModel

class DealsDataSource(
    private val getDealsUseCase: GetDealsUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : PagingSource<Int, DealRenderModel>() {

    override fun getRefreshKey(state: PagingState<Int, DealRenderModel>): Int = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DealRenderModel> {
        val key = params.key ?: 0

        return kotlin.runCatching {
            val (_, deals) = getDealsUseCase.invoke(
                PageParameter(
                    offset = key,
                    pageSize = params.loadSize
                )
            )
            LoadResult.Page(
                data = deals.map {
                    it.toRenderModel(
                        resourcesProvider,
                        R.color.newPriceColor,
                        R.color.oldPriceColor
                    )
                },
                prevKey = null,
                nextKey = key + params.loadSize
            )
        }.getOrElse { throwable ->
            LoadResult.Error(throwable)
        }
    }
}
