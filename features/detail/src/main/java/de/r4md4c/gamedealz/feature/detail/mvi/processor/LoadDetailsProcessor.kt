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

import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Status
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentArgs
import de.r4md4c.gamedealz.feature.detail.PriceDetails
import de.r4md4c.gamedealz.feature.detail.R
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviResult
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.ErrorResult
import de.r4md4c.gamedealz.feature.detail.mvi.IsAddedToWatchlistResult
import de.r4md4c.gamedealz.feature.detail.mvi.LoadingResult
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import de.r4md4c.gamedealz.feature.detail.mvi.SectionsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

internal class LoadDetailsProcessor @Inject constructor(
    private val detailsFragmentArgs: DetailsFragmentArgs,
    private val getPlainDetails: GetPlainDetails,
    private val stateStore: ModelStore<DetailsViewState>,
    @ForApplication private val resourcesProvider: ResourcesProvider,
    private val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase
) : IntentProcessor<DetailsMviEvent, DetailsViewState> {

    override fun process(viewEvent: Flow<DetailsMviEvent>): Flow<MviResult<DetailsViewState>> =
        listOf(
            viewEvent.filterIsInstance<DetailsMviEvent.InitEvent>(),
            viewEvent.filterIsInstance<DetailsMviEvent.RetryClickEvent>()
        ).merge()
            .map { detailsFragmentArgs.plainId }
            .flatMapLatest { plainId ->
                // When restoring we just watch the watchlist status.
                if (stateStore.currentState.sections.isNotEmpty()) {
                    isGameAddedToWatchListUseCase(TypeParameter(plainId))
                        .map { IsAddedToWatchlistResult(it) }
                } else {
                    getPlainsWithWatchlistStatus(plainId)
                }
            }

    private suspend fun getPlainsWithWatchlistStatus(plainId: String): Flow<DetailsMviResult> =
        getPlainDetails(TypeParameter(GetPlainDetails.Params(plainId)))
            .combine(isGameAddedToWatchListUseCase(TypeParameter(plainId)))
            { plainDetails, isAddedToWatchlist ->
                when (plainDetails.status) {
                    Status.LOADING -> LoadingResult(showLoading = true)
                    Status.SUCCESS -> SectionsResult(
                        newSections = plainDetails.data!!.toSections(),
                        isAddedToWatchlist = isAddedToWatchlist
                    )
                    Status.ERROR -> ErrorResult(errorMessage = plainDetails.message!!)
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
            val allScreenshots = gameArtworkDetails!!.screenshots
            val spanCount = resourcesProvider.getInteger(R.integer.screenshots_span_count)

            result += Section.ScreenshotSection(
                allScreenshots = allScreenshots,
                visibleScreenshots = allScreenshots.take(spanCount)
            )
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
