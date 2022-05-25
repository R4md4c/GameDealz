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

package de.r4md4c.gamedealz.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Status
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.domain.usecase.IsGameAddedToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.RemoveFromWatchlistUseCase
import de.r4md4c.gamedealz.feature.detail.model.PriceDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DetailsViewModel @AssistedInject constructor(
    private val getPlainDetails: GetPlainDetails,
    private val isGameAddedToWatchListUseCase: IsGameAddedToWatchListUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    @ForApplication private val resourcesProvider: ResourcesProvider,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = DetailsFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val isWatchedStateFlow = MutableStateFlow(false)

    private val requestStateStateFlow = MutableStateFlow<RequestState>(RequestState.Loading)

    private val isExpanded = savedStateHandle.getLiveData("isExpanded", false)

    private val sortOrder =
        savedStateHandle.getLiveData<SortOrder>("sortOrder", SortOrder.ByCurrentPrice)

    private val onRefresh = Channel<Unit>(capacity = Channel.UNLIMITED)

    val state by lazy {
        combine(
            isWatchedStateFlow, requestStateStateFlow, isExpanded.asFlow(), sortOrder.asFlow()
        ) { isWatched, requestState, isExpanded, sortOrder ->
            val isLoading = requestState is RequestState.Loading
            val sections = (requestState as? RequestState.Loaded)?.sections?.let {
                prepareSections(sections = it, isExpanded = isExpanded, sortOrder = sortOrder)
            }.orEmpty()
            val error = (requestState as? RequestState.Error)?.cause

            DetailsViewState(
                sections = sections,
                isWatched = isWatched,
                loading = isLoading,
                errorMessage = error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DetailsViewState())
            .also {
                loadGameDetails()
            }
    }

    fun onExpandClicked() {
        val isExpanded = isExpanded.value ?: false
        this.isExpanded.value = !isExpanded
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        this.sortOrder.value = sortOrder
    }

    fun onRemoveFromWatchList() {
        viewModelScope.launch {
            removeFromWatchlistUseCase.invoke(args.plainId)
        }
    }

    fun onRefresh() {
        onRefresh.trySend(Unit)
    }

    private fun loadGameDetails() {
        onRefresh.consumeAsFlow()
            .flatMapLatest {
                getPlainDetails.invoke(
                    args.plainId
                )
            }.combine(isGameAddedToWatchListUseCase.invoke(args.plainId), ::Pair)
            .onEach { (resource, isWatched) ->
                val requestState = when (resource.status) {
                    Status.SUCCESS -> RequestState.Loaded(resource.data?.toSections().orEmpty())
                    Status.ERROR -> RequestState.Error(resource.message.orEmpty())
                    Status.LOADING -> RequestState.Loading
                }
                isWatchedStateFlow.value = isWatched
                requestStateStateFlow.value = requestState
            }.launchIn(viewModelScope)

        onRefresh()
    }

    private fun PlainDetailsModel.toSections(): List<Section> = buildList {
        gameArtworkDetails?.shortDescription?.let { description ->
            this += Section.GameInfoSection(
                imageUrl = gameArtworkDetails!!.headerImage!!,
                description = description
            )
        }

        if (!gameArtworkDetails?.screenshots.isNullOrEmpty()) {
            val allScreenshots = gameArtworkDetails!!.screenshots
            val spanCount = resourcesProvider.getInteger(R.integer.screenshots_span_count)

            this += Section.ScreenshotSection(
                allScreenshots = allScreenshots,
                visibleScreenshots = allScreenshots.take(spanCount)
            )
        }

        val priceDetailsList = shopPrices.map { entry ->
            PriceDetails(
                priceModel = entry.value.priceModel,
                historicalLowModel = entry.value.historicalLowModel,
                shopModel = entry.key,
                currencyModel = currencyModel
            )
        }

        this += Section.PriceSection(priceDetails = priceDetailsList)
    }

    private fun prepareSections(
        sections: List<Section>,
        isExpanded: Boolean,
        sortOrder: SortOrder
    ): List<Section> {
        val newScreenshotSection = prepareScreenshotSection(sections, isExpanded)

        val priceSection = preparePriceSection(sections, sortOrder)

        return sections.mapNotNull { aSection ->
            if (aSection is Section.ScreenshotSection) {
                newScreenshotSection
            } else if (aSection is Section.PriceSection && priceSection != null) {
                priceSection
            } else {
                aSection
            }
        }
    }

    private fun preparePriceSection(
        sections: List<Section>,
        sortOrder: SortOrder
    ): Section.PriceSection? {
        return sections.filterIsInstance<Section.PriceSection>().firstOrNull()
            ?.let { priceSection ->
                priceSection.copy(
                    currentSortOrder = sortOrder,
                    priceDetails = sortPricesBy(priceSections = priceSection, sortType = sortOrder)
                )
            }
    }

    private fun prepareScreenshotSection(
        sections: List<Section>,
        isExpanded: Boolean
    ): Section.ScreenshotSection? {
        val screenshotsSection =
            sections.filterIsInstance<Section.ScreenshotSection>().firstOrNull() ?: return null
        val spanCount =
            resourcesProvider.getInteger(screenshotsSection.visibleItemsInSection)

        val newScreenshotSection = if (isExpanded) {
            screenshotsSection.copy(
                isExpanded = true,
                visibleScreenshots = screenshotsSection.allScreenshots
            )
        } else {
            screenshotsSection.copy(
                isExpanded = false,
                visibleScreenshots = screenshotsSection.allScreenshots.take(spanCount)
            )
        }
        return newScreenshotSection
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

    private sealed class RequestState {
        object Loading : RequestState()
        class Loaded(val sections: List<Section>) : RequestState()
        class Error(val cause: String) : RequestState()
    }

    @AssistedFactory
    interface Factory {
        fun create(handle: SavedStateHandle): DetailsViewModel
    }

    companion object {
        private const val STOP_TIMEOUT = 5000L
    }
}
