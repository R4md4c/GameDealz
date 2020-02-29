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

package de.r4md4c.gamedealz.feature.watchlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.Resource
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class AddToWatchListViewModelTest {

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(CoroutinesTestRule())
        .around(InstantTaskExecutorRule())

    @Mock
    private lateinit var resourcesProvider: ResourcesProvider

    @Mock
    private lateinit var getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var getPlainDetails: GetPlainDetails

    @Mock
    private lateinit var addToWatchListUseCase: AddToWatchListUseCase

    private val savedStateHandle = SavedStateHandle(mutableMapOf())

    private lateinit var viewModel: AddToWatchListViewModel

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        savedStateHandle.keys().forEach { savedStateHandle.remove<Any>(it) }
    }

    @Test
    fun `should post show loading UIEvent when use case emits loading resource`() {
        val arrangeBuilder = ArrangeBuilder()
            .arrange()

        arrangeBuilder.emitLoading()

        assertThat(viewModel.uiEvents.value)
            .isEqualTo(AddToWatchListViewModel.UIEvent.ShowLoading)
    }

    @Test
    fun `should post hide loading when use case emits data`() {
        val arrangeBuilder = ArrangeBuilder()
            .arrange()

        arrangeBuilder.emitData(Fixtures.plainDetailsModel(PLAIN_ID))

        assertThat(viewModel.uiEvents.value)
            .isEqualTo(AddToWatchListViewModel.UIEvent.HideLoading)
    }

    @Test
    fun `should post error use case emits error resource`() {
        val arrangeBuilder = ArrangeBuilder()
            .arrange()

        arrangeBuilder.emitError()

        assertThat(viewModel.uiEvents.value)
            .isEqualTo(AddToWatchListViewModel.UIEvent.ShowError("Error"))
    }

    @Test
    fun `should post correct initial ui model when use case emits data`() {
        val arrangeBuilder = ArrangeBuilder()
            .arrange()

        arrangeBuilder.emitData(Fixtures.plainDetailsModel(PLAIN_ID))

        assertThat(viewModel.addToWatchlistUIModel.value)
            .isEqualTo(
                AddToWatchlistUIModel(
                    areAllStoresMarked = true,
                    shopPrices = emptyMap(),
                    availableStores = emptyList(),
                    toggledStoreMap = emptyMap()
                )
            )
    }

    @Test
    fun formatPrice() {
        // Try Deutsch since this reproduced the problem
        Locale.setDefault(
            Locale(
                "de",
                "DE"
            )
        )
        ArrangeBuilder()
            .withCurrencyCode("USD")
            .arrange()

        assertThat(viewModel.formatPrice("1")).isEqualTo("$0.01")
        assertThat(viewModel.formatPrice("11")).isEqualTo("$0.11")
        assertThat(viewModel.formatPrice("1123")).isEqualTo("$11.23")
    }

    inner class ArrangeBuilder {

        private val plainDetailsChannel = Channel<Resource<PlainDetailsModel>>()

        init {
            savedStateHandle.set("plainId", PLAIN_ID)
            whenever(getPlainDetails.invoke(anyOrNull())).thenReturn(plainDetailsChannel.consumeAsFlow())
        }

        fun withCurrencyCode(currencyCode: String) = apply {
            runBlocking {
                whenever(
                    getCurrentActiveRegionUseCase.invoke()
                )
                    .thenReturn(
                        ActiveRegion(
                            "",
                            CountryModel(""),
                            CurrencyModel(
                                currencyCode,
                                ""
                            )
                        )
                    )
            }
        }

        fun emitLoading() = apply {
            plainDetailsChannel.offer(Resource.loading(null))
        }

        fun emitData(priceDetailsModel: PlainDetailsModel) = apply {
            plainDetailsChannel.offer(Resource.success(priceDetailsModel))
        }

        fun emitError() = apply {
            plainDetailsChannel.offer(Resource.error("Error", null))
        }

        fun arrange() = apply {
            viewModel = AddToWatchListViewModel(
                savedStateHandle,
                getPlainDetails,
                TestDispatchers,
                getCurrentActiveRegionUseCase,
                resourcesProvider,
                addToWatchListUseCase
            )
        }
    }
}

private const val PLAIN_ID = "aPlain"
