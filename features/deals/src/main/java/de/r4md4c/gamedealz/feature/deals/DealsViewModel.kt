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

package de.r4md4c.gamedealz.feature.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.impl.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.feature.deals.datasource.DealsDataSource
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class DealsViewModel @Inject constructor(
    private val getDealsUseCase: GetDealsUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider,
    selectedStoresUseCase: GetSelectedStoresUseCase,
) : ViewModel() {

    val pager = selectedStoresUseCase().flatMapLatest {
        Pager(
            PagingConfig(
                pageSize = BuildConfig.DEFAULT_PAGE_SIZE,
                initialLoadSize = BuildConfig.DEFAULT_PAGE_SIZE * RATIO,
                enablePlaceholders = false
            )
        ) {
            DealsDataSource(getDealsUseCase, resourcesProvider)
        }.flow.cachedIn(viewModelScope)
    }

    companion object {
        private const val RATIO = 2
    }
}
