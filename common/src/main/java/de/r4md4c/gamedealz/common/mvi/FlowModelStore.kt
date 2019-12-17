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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

open class FlowModelStore<S : MviState>(startingState: S) : ModelStore<S>,
    CoroutineScope by MainScope() {

    private val intents: Channel<Intent<S>> = Channel()
    private val store = ConflatedBroadcastChannel(startingState)

    init {
        val t = this
        launch {
            while (isActive) {
                println("Store Instance: $t")
                store.offer(intents.receive().reduce(store.value))
            }
        }
    }

    override suspend fun process(intent: Intent<S>) {
        intents.send(intent)
    }

    override fun modelState(): Flow<S> = store.asFlow()
}
