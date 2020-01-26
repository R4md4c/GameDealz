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

package de.r4md4c.gamedealz.common.mvi

import de.r4md4c.gamedealz.test.FlowRecorder
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.test.recordWith
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

private sealed class TestMviViewEvent : MviViewEvent {
    object Event1 : TestMviViewEvent()
    object Event2 : TestMviViewEvent()
}

private data class TestState(val aField: String = "") : MviState

private class TestMviViewModel(
    intentProcessors: Set<@JvmSuppressWildcards IntentProcessor<TestMviViewEvent, TestState>>,
    store: ModelStore<TestState>
) : BaseMviViewModel<TestMviViewEvent, TestState>(intentProcessors, store)

@RunWith(RobolectricTestRunner::class)
class BaseMviViewModelTest {

    @Mock
    private lateinit var mockStore: ModelStore<TestState>

    private lateinit var testMviViewModel: TestMviViewModel

    @Before
    fun beforeEach() {
        MockitoAnnotations.initMocks(this)

        testMviViewModel = TestMviViewModel(setOf(
            createProcessor {
                it.filterIsInstance<TestMviViewEvent.Event1>()
                    .map { createResult<TestState> { copy(aField = aField + "Event1 ") } }
            },
            createProcessor {
                it.filterIsInstance<TestMviViewEvent.Event2>()
                    .map { createResult<TestState> { copy(aField = aField + "Event2 ") } }
            }
        ), FlowModelStore(TestState(), TestDispatchers))
    }

    @Test
    fun `it should work correctly when events are emitted`() = runBlockingTest {
        val flowRecorder = FlowRecorder<TestState>(this)
        val job = testMviViewModel.modelState.recordWith(flowRecorder)

        testMviViewModel.onViewEvents(
            flowOf(TestMviViewEvent.Event1, TestMviViewEvent.Event2),
            this
        )

        assertThat(flowRecorder.drop(1)).hasSize(2)
        assertThat(flowRecorder.drop(1)).isEqualTo(
            listOf(TestState("Event1 "), TestState("Event1 Event2 "))
        )
        job.cancelAndJoin()
    }

    @Test
    fun `it should handle thousands of events gracefully`() = runBlockingTest {
        val flowRecorder = FlowRecorder<TestState>(this)
        val job = testMviViewModel.modelState.recordWith(flowRecorder)

        testMviViewModel.onViewEvents(flow {
            repeat(2000) { emit(TestMviViewEvent.Event1) }
        }, this)

        assertThat(flowRecorder.drop(1)).hasSize(2000)
        assertThat(flowRecorder.last().aField).endsWith("Event1 ")
        job.cancelAndJoin()
    }

    private inline fun createProcessor(
        crossinline block: (Flow<TestMviViewEvent>) -> Flow<MviResult<TestState>>
    ): IntentProcessor<TestMviViewEvent, TestState> =
        object : IntentProcessor<TestMviViewEvent, TestState> {
            override fun process(viewEvent: Flow<TestMviViewEvent>): Flow<MviResult<TestState>> =
                block(viewEvent)
        }

    private inline fun <T : MviState> createResult(crossinline block: T.() -> T): MviResult<T> =
        object : ReducibleMviResult<T> {
            override fun reduce(oldState: T): T = oldState.block()
        }
}
