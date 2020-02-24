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
import de.r4md4c.gamedealz.test.recordWith
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ChannelUIEventsDispatcherTest {

    private sealed class TestUIEvent : UIEvent {
        object Success : TestUIEvent()
        object Failed : TestUIEvent()
    }

    private lateinit var eventsDispatcher: ChannelUIEventsDispatcher<TestUIEvent>

    @Before
    fun beforeEach() {
        eventsDispatcher = ChannelUIEventsDispatcher()
    }

    @Test
    fun `it should buffer sent events when unsubscribed`() = runBlockingTest {
        val flowRecorder = FlowRecorder<UIEvent>(this)
        eventsDispatcher.dispatchEvent(TestUIEvent.Success)
        eventsDispatcher.dispatchEvent(TestUIEvent.Success)
        eventsDispatcher.dispatchEvent(TestUIEvent.Failed)

        val job = eventsDispatcher.uiEvents.recordWith(flowRecorder)

        assertThat(flowRecorder.iterator()).hasSize(3)
        job.cancelAndJoin()
    }
}
