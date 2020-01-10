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

import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.verify
import de.r4md4c.gamedealz.domain.usecase.ToggleNightModeUseCase
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NightModeToggleIntentProcessorTest {

    @Mock
    lateinit var useCase: ToggleNightModeUseCase

    @InjectMocks
    private lateinit var processor: NightModeToggleIntentProcessor

    @Test
    fun `process calls toggle use case without emitting any`() = runBlockingTest {
        val result = processor.process(flowOf(HomeMviViewEvent.NightModeToggleViewEvent))
            .toCollection(mutableListOf())

        assertThat(result).isEmpty()
        verify(useCase).invoke(isNull())
    }
}
