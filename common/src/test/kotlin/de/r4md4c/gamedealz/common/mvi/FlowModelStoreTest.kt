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

import de.r4md4c.gamedealz.test.CoroutinesTestRule
import de.r4md4c.gamedealz.test.FlowRecorder
import de.r4md4c.gamedealz.test.TestDispatchers
import de.r4md4c.gamedealz.test.recordWith
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FlowModelStoreTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private lateinit var flowModelStore: FlowModelStore<TestState>

    @Before
    fun beforeEach() {
        flowModelStore = FlowModelStore(TestDispatchers, createSimpleStateFactory { TestState() })
    }

    @Test
    fun `should emit initial state correctly`() = runBlockingTest {
        val recorder = FlowRecorder<TestState>(this)
        val job = flowModelStore.modelState().recordWith(recorder)

        assertThat(recorder.takeValue()).isEqualTo(TestState())
        recorder.assertNoMoreValues()
        job.cancelAndJoin()
    }

    @Test
    fun `should reduce result of type Reducible correctly`() = runBlockingTest {
        val recorder = FlowRecorder<TestState>(this)
        val job = flowModelStore.modelState().recordWith(recorder)
        recorder.clearValues()

        flowModelStore.process(createReducibleResult { this.copy(field = "field2") })

        assertThat(recorder.takeValue()).isEqualTo(TestState(field = "field2"))
        recorder.assertNoMoreValues()
        job.cancelAndJoin()
    }

    @Test
    fun `should not emit anything when result is not of type Reducible`() = runBlockingTest {
        val recorder = FlowRecorder<TestState>(this)
        val job = flowModelStore.modelState().recordWith(recorder)
        recorder.clearValues()

        flowModelStore.process(createEmptyResult())

        recorder.assertNoMoreValues()
        job.cancelAndJoin()
    }

    @Test
    fun `should not emit anything when result is reduced with same value`() = runBlockingTest {
        val recorder = FlowRecorder<TestState>(this)
        val job = flowModelStore.modelState().recordWith(recorder)
        recorder.clearValues()

        flowModelStore.process(createReducibleResult { copy(field = "field2") })
        flowModelStore.process(createReducibleResult { copy(field = "field2") })

        assertThat(recorder.takeValue()).isEqualTo(TestState(field = "field2"))
        recorder.assertNoMoreValues()
        job.cancelAndJoin()
    }

    private inline fun createReducibleResult(crossinline block: TestState.() -> TestState) =
        object : ReducibleMviResult<TestState> {
            override fun reduce(oldState: TestState): TestState = oldState.run(block)
        }

    private fun createEmptyResult() = object : MviResult<TestState> {}

    private data class TestState(val field: String = "field1") : MviState
}
