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

import de.r4md4c.gamedealz.common.IDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

class ChannelUIEventsDispatcher<T : UIEvent> @Inject constructor(
    dispatchers: IDispatchers
) : UIEventsDispatcher<T> {

    private val dispatcherScope = CoroutineScope(SupervisorJob() + dispatchers.Main)

    private val eventsChannel = Channel<T>(capacity = UNLIMITED)

    override val uiEvents: Flow<T> =
        eventsChannel.consumeAsFlow().broadcastIn(dispatcherScope).asFlow()

    override fun dispatchEvent(event: T) {
        eventsChannel.offer(event)
    }

    override fun onClear() {
        dispatcherScope.cancel()
    }
}
