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

package de.r4md4c.gamedealz.feature.home.mvi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.r4md4c.gamedealz.common.mvi.intent
import de.r4md4c.gamedealz.common.mvi.uiSideEffect
import de.r4md4c.gamedealz.feature.home.R
import de.r4md4c.gamedealz.feature.home.mvi.intent.InitIntent
import de.r4md4c.gamedealz.feature.home.mvi.intent.LogoutIntent
import de.r4md4c.gamedealz.feature.home.mvi.intent.NightModeToggleIntent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import de.r4md4c.gamedealz.feature.home.state.HomeUiSideEffect
import de.r4md4c.gamedealz.test.mvi.FakeModelStore
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class HomeMviIntentsProcessorTest {

    @Mock
    private lateinit var initIntentFactory: InitIntent.Factory

    @Mock
    private lateinit var nightModeToggleIntent: NightModeToggleIntent.Factory

    @Mock
    private lateinit var logoutIntentFactory: LogoutIntent.Factory

    private val store = FakeModelStore(HomeMviViewState())

    private lateinit var testSubject: HomeMviIntentsProcessor

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        testSubject = HomeMviIntentsProcessor(
            store,
            initIntentFactory, nightModeToggleIntent, logoutIntentFactory
        )
    }

    @Test
    fun initViewEvent() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.InitViewEvent(this)) {
            assertThat(it).isEqualTo(listOf(HomeMviViewState()))
            verify(initIntentFactory).create(any(), any())
        }
    }

    @Test
    fun nightModeToggleViewEvent() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.NightModeToggleViewEvent(this)) {
            assertThat(it).isEqualTo(listOf(HomeMviViewState()))
            verify(nightModeToggleIntent).create(any())
        }
    }

    @Test
    fun logoutViewEvent() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.LogoutViewEvent) {
            assertThat(it).isEqualTo(listOf(HomeMviViewState()))
            verify(logoutIntentFactory).create(any())
        }
    }

    @Test
    fun `loginViewEvent sends StartAuthenticationFlow UiSideEffect`() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.LoginViewEvent) {
            assertThat(it).isEqualTo(
                listOf(
                    HomeMviViewState().copy(
                        uiSideEffect = uiSideEffect { HomeUiSideEffect.StartAuthenticationFlow }
                    )
                )
            )
        }
    }

    @Test
    fun `NavigateToOngoingDealsScreen sends NavigateSideEffect`() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.NavigateToOngoingDealsScreen) {
            assertThat(it.first().uiSideEffect).isNotNull()
            assertThat(it.first().uiSideEffect!!.peek())
                .isEqualToComparingFieldByField(
                    HomeUiSideEffect.NavigateSideEffect(
                        R.id.dealsFragment,
                        true
                    )
                )
        }
    }

    @Test
    fun `NavigateToManageWatchlistScreen sends NavigateSideEffect`() = runBlockingTest {
        executeWithAssertBlock(HomeMviViewEvent.NavigateToManageWatchlistScreen) {
            assertThat(it.first().uiSideEffect).isNotNull()
            assertThat(it.first().uiSideEffect!!.peek())
                .isEqualToComparingFieldByField(
                    HomeUiSideEffect.NavigateSideEffect(
                        R.id.manageWatchlistFragment,
                        false
                    )
                )
        }
    }

    private inline fun executeWithAssertBlock(
        event: HomeMviViewEvent,
        crossinline block: (List<HomeMviViewState>) -> Unit
    ) = runBlockingTest {
        ArrangeBuilder()
        val collector = mutableListOf<HomeMviViewState>()
        testSubject.process(event)

        val job = launch { store.modelState().toCollection(collector) }

        block(collector)
        job.cancel()
    }

    inner class ArrangeBuilder {
        init {
            whenever(initIntentFactory.create(any(), any())).thenReturn(intent { copy() })
            whenever(nightModeToggleIntent.create(any())).thenReturn(intent { copy() })
            whenever(logoutIntentFactory.create(any())).thenReturn(intent { copy() })
        }
    }
}
