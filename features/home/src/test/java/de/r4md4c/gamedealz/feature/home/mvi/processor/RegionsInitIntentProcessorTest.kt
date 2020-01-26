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

package de.r4md4c.gamedealz.feature.home.mvi.processor

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.CurrencyModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.OnCurrentActiveRegionReactiveUseCase
import de.r4md4c.gamedealz.feature.home.mvi.ActiveRegionResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviResult
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.test.FlowRecorder
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.test.recordWith
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegionsInitIntentProcessorTest {

    @Mock
    private lateinit var activeRegionUseCase: GetCurrentActiveRegionUseCase

    @Mock
    private lateinit var onRegionChangeUseCase: OnCurrentActiveRegionReactiveUseCase

    @Mock
    private lateinit var getStoresUseCase: GetStoresUseCase

    private lateinit var processor: RegionsInitIntentProcessor

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        processor = RegionsInitIntentProcessor(
            activeRegionUseCase,
            onRegionChangeUseCase,
            getStoresUseCase,
            TestDispatchers
        )
    }

    @Test
    fun `should emit new results with changed active region when active region change `() =
        runBlockingTest {
            val channel = Channel<ActiveRegion>()
            ArrangeBuilder()
                .withActiveRegionFromReactive(channel)
                .withGetCurrentActiveRegion(activeRegion())

            val flowRecorder = FlowRecorder<HomeMviResult>(this)
            processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).recordWith(flowRecorder)
            channel.offer(activeRegion(regionCode = "US"))
            channel.offer(activeRegion(regionCode = "EU"))

            val result = flowRecorder.toList().takeLast(2)
            assertThat(result).hasSize(2)
            assertThat(result).isEqualTo(
                listOf(
                    ActiveRegionResult(region = activeRegion("US")),
                    ActiveRegionResult(region = activeRegion("EU"))
                )
            )
            channel.close()
        }

    @Test
    fun `should emit new state with activeRegionUseCase returns`() =
        runBlockingTest {
            ArrangeBuilder()
                .withActiveRegionFromReactive(activeRegion())
                .withGetCurrentActiveRegion(activeRegion())

            val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).toCollection(
                mutableListOf()
            )

            assertThat(result.last()).isEqualTo(ActiveRegionResult(activeRegion()))
        }

    @Test
    fun `should call getStoresUseCase when reactive active region emits`() = runBlockingTest {
        ArrangeBuilder()
            .withActiveRegionFromReactive(activeRegion())
            .withGetCurrentActiveRegion(activeRegion())

        val result = processor.process(flowOf(HomeMviViewEvent.InitViewEvent)).toCollection(
            mutableListOf()
        )

        verify(getStoresUseCase).invoke(TypeParameter(activeRegion()))
    }

    private fun Intent<HomeMviViewState>.reduce() = this.reduce(HomeMviViewState())

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

        fun withActiveRegionFromReactive(channel: Channel<ActiveRegion>) = apply {
            runBlockingTest {
                whenever(onRegionChangeUseCase.activeRegionChange()).thenReturn(channel.consumeAsFlow())
            }
        }

        fun withGetCurrentActiveRegion(activeRegion: ActiveRegion) = apply {
            runBlockingTest {
                whenever(activeRegionUseCase.invoke(anyOrNull())).thenReturn(activeRegion)
            }
        }
    }
}
