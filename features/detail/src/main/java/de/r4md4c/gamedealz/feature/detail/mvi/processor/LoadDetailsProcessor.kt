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

import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Status
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.feature.detail.PriceDetails
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.ErrorResult
import de.r4md4c.gamedealz.feature.detail.mvi.LoadingResult
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import de.r4md4c.gamedealz.feature.detail.mvi.SectionsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

internal class LoadDetailsProcessor @Inject constructor(
    private val getPlainDetails: GetPlainDetails,
    private val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase
) : IntentProcessor<DetailsMviEvent, DetailsViewState> {

    override fun process(viewEvent: Flow<DetailsMviEvent>): Flow<MviResult<DetailsViewState>> =
        viewEvent.filterIsInstance<DetailsMviEvent.InitEvent>()
            .flatMapConcat {
                getPlainDetails(TypeParameter(GetPlainDetails.Params(it.plainId)))
            }.transformLatest {
                when (it.status) {
                    Status.LOADING -> emit(LoadingResult(showLoading = true))
                    Status.SUCCESS -> emit(SectionsResult(it.data!!.toSections()))
                    Status.ERROR -> emit(ErrorResult(it.message!!))
                }
            }

    private fun PlainDetailsModel.toSections(): List<Section> {
        val result = mutableListOf<Section>()

        gameArtworkDetails?.shortDescription?.let { description ->
            result += Section.GameInfoSection(
                imageUrl = gameArtworkDetails!!.headerImage!!,
                description = description
            )
        }

        if (gameArtworkDetails?.screenshots?.isNullOrEmpty() == false) {
            result += Section.ScreenshotSection(screenshots = gameArtworkDetails!!.screenshots)
        }

        val priceDetailsList = shopPrices.map {
            PriceDetails(
                priceModel = it.value.priceModel,
                historicalLowModel = it.value.historicalLowModel,
                shopModel = it.key,
                currencyModel = currencyModel
            )
        }

        result += Section.PriceSection(priceDetails = priceDetailsList)

        return result.toList()
    }
}
