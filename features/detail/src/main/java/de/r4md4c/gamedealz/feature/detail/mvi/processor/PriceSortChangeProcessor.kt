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

package de.r4md4c.gamedealz.feature.detail.mvi.processor

import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.feature.detail.model.PriceDetails
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import de.r4md4c.gamedealz.feature.detail.mvi.SortOrder
import de.r4md4c.gamedealz.feature.detail.mvi.SortPricesResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

internal class PriceSortChangeProcessor @Inject constructor(
    private val stateStore: ModelStore<DetailsViewState>
) : IntentProcessor<DetailsMviEvent, DetailsViewState> {

    override fun process(
        viewEvent: Flow<DetailsMviEvent>
    ): Flow<MviResult<DetailsViewState>> =
        viewEvent.filterIsInstance<DetailsMviEvent.PriceFilterChangeEvent>()
            .filter {
                val currentState = stateStore.currentState
                currentState.sections
                    .filterIsInstance<Section.PriceSection>()
                    .firstOrNull()
                    ?.currentSortOrder != it.sortOrder
            }.transformLatest { filterEvent ->
                val priceSection = stateStore.currentState.sections
                    .filterIsInstance<Section.PriceSection>().firstOrNull()
                    ?: return@transformLatest

                val newlySortedPrices = sortPricesBy(priceSection, filterEvent.sortOrder)

                emit(
                    SortPricesResult(
                        newPricesSection = priceSection.copy(
                            currentSortOrder = filterEvent.sortOrder,
                            priceDetails = newlySortedPrices
                        )
                    )
                )
            }

    private fun sortPricesBy(
        priceSections: Section.PriceSection,
        sortType: SortOrder
    ): List<PriceDetails> = priceSections.priceDetails.sortedBy {
        when (sortType) {
            SortOrder.ByCurrentPrice -> it.priceModel.newPrice
            SortOrder.ByHistoricalLow -> it.historicalLowModel?.price
        }.exhaustive
    }
}
