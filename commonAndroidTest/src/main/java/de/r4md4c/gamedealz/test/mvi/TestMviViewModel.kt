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

package de.r4md4c.gamedealz.test.mvi

import de.r4md4c.gamedealz.common.mvi.MviState
import de.r4md4c.gamedealz.common.mvi.MviViewEvent
import de.r4md4c.gamedealz.common.mvi.MviViewModel
import de.r4md4c.gamedealz.common.mvi.UIEvent
import de.r4md4c.gamedealz.common.mvi.UIEventsDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TestMviViewModel<State : MviState, Event : MviViewEvent, UISideEffect : UIEvent>(
    private val initialState: State
) : MviViewModel<State, Event>, UIEventsDispatcher<UISideEffect> {

    private val sideEffectsChannel = Channel<UISideEffect>()

    private val eventsList = mutableListOf<Event>()

    private val conflatedBroadcastChannel = MutableStateFlow(initialState)

    val recordedEvents = eventsList.toList()

    fun emitState(state: State) {
        conflatedBroadcastChannel.value = state
    }

    override val uiEvents: Flow<UISideEffect>
        get() = sideEffectsChannel.consumeAsFlow()

    override fun dispatchEvent(event: UISideEffect) {
        sideEffectsChannel.trySend(event)
    }

    override fun onClear() {
        throw UnsupportedOperationException("TestMviViewModel does not support clearing")
    }

    override val modelState: Flow<State>
        get() = conflatedBroadcastChannel.asStateFlow()

    override fun onViewEvents(viewEventFlow: Flow<Event>, viewScope: CoroutineScope) {
        viewEventFlow.onEach { eventsList += it }.launchIn(viewScope)
    }

    override fun onCleared() {
        throw UnsupportedOperationException("TestMviViewModel does not support clearing")
    }
}
