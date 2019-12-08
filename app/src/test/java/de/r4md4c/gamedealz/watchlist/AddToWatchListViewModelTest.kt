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

package de.r4md4c.gamedealz.watchlist

import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.usecase.AddToWatchListUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.TestDispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class AddToWatchListViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var resourcesProvider: ResourcesProvider

    @Mock
    private lateinit var getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var getStoresUseCase: GetStoresUseCase

    @Mock
    private lateinit var addToWatchListUseCase: AddToWatchListUseCase

    private lateinit var viewModel: de.r4md4c.gamedealz.feature.AddToWatchListViewModel

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        viewModel = de.r4md4c.gamedealz.feature.AddToWatchListViewModel(
            TestDispatchers,
            resourcesProvider,
            getCurrentActiveRegionUseCase,
            getStoresUseCase,
            addToWatchListUseCase
        )
    }

    @Test
    fun formatPrice() {
        // Try Deutsch since this reproduced the problem
        Locale.setDefault(Locale("de", "DE"))
        ArrangeBuilder()
            .withCurrencyCode("USD")
            .arrange()

        assertThat(viewModel.formatPrice("1")).isEqualTo("$0.01")
        assertThat(viewModel.formatPrice("11")).isEqualTo("$0.11")
        assertThat(viewModel.formatPrice("1123")).isEqualTo("$11.23")
    }

    inner class ArrangeBuilder {
        fun withCurrencyCode(currencyCode: String) = apply {
            runBlocking {
                whenever(getCurrentActiveRegionUseCase.invoke())
                    .thenReturn(ActiveRegion("", CountryModel(""), CurrencyModel(currencyCode, "")))
            }
        }

        fun arrange() {
            viewModel.loadStores()
        }
    }
}
