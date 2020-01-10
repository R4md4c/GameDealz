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
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.domain.usecase.LogoutUseCase
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LogoutIntentProcessorTest {

    @Mock
    lateinit var logoutUseCase: LogoutUseCase

    @InjectMocks
    internal lateinit var testSubject: LogoutIntentProcessor

    @Test
    fun `should emit new state with user has logged out notification`() = runBlockingTest {
        val result = testSubject.process(flowOf(HomeMviViewEvent.LogoutViewEvent)).first()

        assertThat(result.reduce(HomeMviViewState())).isEqualTo(
            HomeMviViewState().copy(
                uiSideEffect = uiSideEffect { HomeUiSideEffect.NotifyUserHasLoggedOut }
            ))
        verify(logoutUseCase).invoke(anyOrNull())
    }

    @Test
    fun `does nothing when event is not LogoutViewEvent`() = runBlockingTest {
        testSubject.process(flowOf(HomeMviViewEvent.LoginViewEvent)).collect()

        verifyZeroInteractions(logoutUseCase)
    }
}
