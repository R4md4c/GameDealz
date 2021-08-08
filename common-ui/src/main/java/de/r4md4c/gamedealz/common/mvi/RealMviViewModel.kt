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

import de.r4md4c.gamedealz.common.unsafeLazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn

class RealMviViewModel<Event : MviViewEvent, State : MviState>(
    intentProcessors: Set<@JvmSuppressWildcards IntentProcessor<Event, State>>,
    private val store: ModelStore<State>,
    initEvent: Event? = null
) : MviViewModel<State, Event> {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val eventsChannel = Channel<Event>(UNLIMITED)

    init {
        val eventsFlow = eventsChannel.consumeAsFlow()
            .shareIn(viewModelScope, SharingStarted.Lazily)
            .onStart {
                initEvent?.let { emit(it) }
            }

        intentProcessors.map { it.process(eventsFlow) }
            .merge()
            .onEach { store.process(it) }
            .launchIn(viewModelScope)
    }

    override val modelState by unsafeLazy { store.modelState() }

    override fun onViewEvents(viewEventFlow: Flow<Event>, viewScope: CoroutineScope) {
        viewEventFlow.onEach { eventsChannel.send(it) }.launchIn(viewScope)
    }

    override fun onCleared() {
        eventsChannel.cancel()
        viewModelScope.cancel()
        store.dispose()
    }
}
