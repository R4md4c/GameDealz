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

package de.r4md4c.gamedealz.feature.home.mvi.intent.helper

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.test.mvi.FakeModelStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegionsInitIntentHelperTest {

    @Mock
    private lateinit var activeRegionUseCase: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase

    @Mock
    private lateinit var getStoresUseCase: GetStoresUseCase

    private val dispatchers: IDispatchers = TestDispatchers

    private lateinit var fakeStore: FakeModelStore<HomeMviViewState>

    private lateinit var testSubject: RegionsInitIntentHelper

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        fakeStore = FakeModelStore(HomeMviViewState())
        testSubject = RegionsInitIntentHelper(
            activeRegionUseCase,
            onRegionChangeUseCase,
            getStoresUseCase,
            dispatchers
        )
    }

    @Test
    fun `should emit new state with changed active region when active region change `() =
        runBlockingTest {
            ArrangeBuilder()
                .withActiveRegionFromReactive(activeRegion())

            with(testSubject) { observeRegions(fakeStore) }

            assertThat(fakeStore.lastValue().activeRegion).isEqualTo(activeRegion())
        }

    @Test
    fun `should emit new state with activeRegionUseCase returns`() =
        runBlockingTest {
            ArrangeBuilder()
                .withActiveRegionFromReactive(activeRegion())
                .withGetCurrentActiveRegion(activeRegion())

            with(testSubject) { observeRegions(fakeStore) }

            assertThat(fakeStore.lastValue().activeRegion).isEqualTo(activeRegion())
        }

    @Test
    fun `should call getStoresUseCase when reactive active region emits`() = runBlockingTest {
        ArrangeBuilder()
            .withActiveRegionFromReactive(activeRegion())

        with(testSubject) { observeRegions(fakeStore) }

        verify(getStoresUseCase).invoke(TypeParameter(activeRegion()))
    }

    private fun activeRegion(regionCode: String = "US") = ActiveRegion(
        regionCode = regionCode,
        currency = CurrencyModel(currencyCode = "Code", sign = "Sign"),
        country = CountryModel(code = "Code")
    )

    inner class ArrangeBuilder {
        init {
            runBlockingTest {
                whenever(getStoresUseCase.invoke(anyOrNull())).thenReturn(flowOf(emptyList()))
            }
        }

        fun withActiveRegionFromReactive(activeRegion: ActiveRegion) = apply {
            runBlockingTest {
                whenever(onRegionChangeUseCase.activeRegionChange()).thenReturn(
                    flowOf(
                        activeRegion
                    )
                )
            }
        }

        fun withGetCurrentActiveRegion(activeRegion: ActiveRegion) = apply {
            runBlockingTest {
                whenever(activeRegionUseCase.invoke(anyOrNull())).thenReturn(activeRegion)
            }
        }
    }
}
