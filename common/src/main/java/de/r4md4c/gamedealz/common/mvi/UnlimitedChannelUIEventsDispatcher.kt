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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class UnlimitedChannelUIEventsDispatcher<T : UIEvent> : UIEventsDispatcher<T> {

    private val eventsChannel = Channel<T>(capacity = UNLIMITED)

    override val uiEvents: Flow<T> = eventsChannel.consumeAsFlow()

    override fun dispatchEvent(event: T) {
        if (eventsChannel.isClosedForSend) {
            return
        }
        eventsChannel.offer(event)
    }

    override fun clear() {
        eventsChannel.close()
    }
}
