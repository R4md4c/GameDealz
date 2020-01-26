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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

open class FlowModelStore<S : MviState>(
    startingState: S,
    dispatchers: IDispatchers
) : ModelStore<S>,
    CoroutineScope by MainScope() {

    private val logger = LoggerFactory.getLogger(FlowModelStore::class.java.simpleName)

    private val intents: Channel<MviResult<S>> = Channel()
    private val store = ConflatedBroadcastChannel(startingState)

    init {
        launch {
            while (isActive) {
                val result = intents.receive()
                val newState = withContext(dispatchers.Default) {
                    when (result) {
                        is ReducibleMviResult<S> -> {
                            logger.debug("State Pre-reduction: ${store.value}")
                            logger.debug("Got Result: $result")
                            result.reduce(store.value).also {
                                logger.debug("State After-reduction: $it")
                            }
                        }
                        else -> null
                    }
                }
                newState?.let(store::offer)
            }
        }
    }

    override suspend fun process(result: MviResult<S>) {
        intents.send(result)
    }

    override fun modelState(): Flow<S> = store.asFlow()

    override fun dispose() {
        cancel()
        intents.cancel()
        store.cancel()
    }
}
