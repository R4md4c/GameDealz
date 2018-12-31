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

package de.r4md4c.gamedealz.common.state

import com.tinder.StateMachine

sealed class State {
    object Loading : State()
    object Idle : State()
    object Empty : State()
    object LoadingMore : State()
    object Error : State()
}

sealed class Event {
    object OnLoadingStart : Event()
    object OnShowEmpty : Event()
    object OnLoadingMoreStarted : Event()
    object OnLoadingMoreEnded : Event()
    data class OnError(val error: Throwable) : Event()
    object OnLoadingEnded : Event()
}

sealed class SideEffect {
    object ShowEmpty : SideEffect()
    object ShowLoading : SideEffect()
    object HideLoading : SideEffect()
    object ShowLoadingMore : SideEffect()
    object HideLoadingMore : SideEffect()
    class ShowError(val error: Throwable) : SideEffect()
}

val UI_STATE_MACHINE = StateMachine.create<State, Event, SideEffect> {
    initialState(State.Idle)

    state<State.Loading> {
        on<Event.OnLoadingEnded> {
            transitionTo(State.Idle, SideEffect.HideLoading)
        }
        on<Event.OnError> {
            transitionTo(State.Error, SideEffect.ShowError(it.error))
        }
        on<Event.OnShowEmpty> {
            transitionTo(State.Empty, SideEffect.ShowEmpty)
        }
    }

    state<State.LoadingMore> {
        on<Event.OnLoadingMoreEnded> {
            transitionTo(State.Idle, SideEffect.HideLoadingMore)
        }
        on<Event.OnLoadingMoreStarted> {
            transitionTo(State.Loading, SideEffect.ShowLoadingMore)
        }
        on<Event.OnError> {
            transitionTo(State.Error, SideEffect.ShowError(it.error))
        }
        on<Event.OnShowEmpty> {
            transitionTo(State.Empty, SideEffect.ShowEmpty)
        }
    }

    state<State.Idle> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
        on<Event.OnError> {
            transitionTo(State.Error, SideEffect.ShowError(it.error))
        }
        on<Event.OnLoadingEnded> {
            transitionTo(State.Idle, SideEffect.HideLoading)
        }
        on<Event.OnLoadingMoreStarted> {
            transitionTo(State.LoadingMore, SideEffect.ShowLoadingMore)
        }
        on<Event.OnLoadingMoreEnded> {
            transitionTo(State.Idle, SideEffect.HideLoadingMore)
        }
        on<Event.OnShowEmpty> {
            transitionTo(State.Empty, SideEffect.ShowEmpty)
        }
    }

    state<State.Empty> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
    }

    state<State.Error> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
    }
}
