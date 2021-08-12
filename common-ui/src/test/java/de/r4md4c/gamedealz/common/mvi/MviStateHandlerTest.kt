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

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import de.r4md4c.gamedealz.test.mvi.FakeModelStore
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MviStateHandlerTest {

    @Parcelize
    internal data class TestState(val counter: Int = 0) : Parcelable, MviState

    private lateinit var testSubject: MviStateHandler<TestState>

    private lateinit var modelStore: FakeModelStore<TestState>

    @Before
    fun beforeEach() {
        modelStore = FakeModelStore(TestState())
        testSubject = MviStateHandler(modelStore)
    }

    @Test
    fun `mviStateHandler stores state correctly when fragment is recreated`() {
        val scenario = ArrangeBuilder()
            .launch()

        scenario.recreate()

        val restoredState = testSubject.createStateRestorer().toRestoredState.value
        assertThat(restoredState).isEqualTo(TestState(counter = 0))
    }

    @Test
    fun `mviStateHandler store state correctly when fragment is recreated and state has changed`() =
        runBlocking {
            val scenario = ArrangeBuilder()
                .launch()

            // Now counter is 1
            modelStore.process(incrementResult())
            scenario.recreate()

            // Assert that count was incremented
            val restoredState = testSubject.createStateRestorer().toRestoredState.value
            assertThat(restoredState).isEqualTo(TestState(counter = 1))
            Unit
        }

    private fun incrementResult() = object : ReducibleMviResult<TestState> {
        override fun reduce(oldState: TestState): TestState =
            oldState.copy(counter = oldState.counter + 1)
    }

    private inner class ArrangeBuilder {

        fun launch(): FragmentScenario<TestFragment> {
            return launchFragmentInContainer {
                TestFragment().apply {
                    mviStateHandler = this@MviStateHandlerTest.testSubject
                }
            }
        }
    }
}

internal class TestFragment : Fragment() {

    lateinit var mviStateHandler: MviStateHandler<MviStateHandlerTest.TestState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(mviStateHandler)
    }
}
