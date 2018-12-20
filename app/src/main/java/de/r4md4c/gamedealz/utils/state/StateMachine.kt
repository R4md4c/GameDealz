package de.r4md4c.gamedealz.utils.state

import com.tinder.StateMachine

sealed class State {
    object Loading : State()
    object Idle : State()
    object Error : State()
}

sealed class Event {
    object OnLoadingStart : Event()
    object OnError : Event()
    object OnLoadingEnded : Event()
}

sealed class SideEffect {
    object ShowLoading : SideEffect()
    object HideLoading : SideEffect()
    object ShowError : SideEffect()
}

val UI_STATE_MACHINE = StateMachine.create<State, Event, SideEffect> {
    initialState(State.Idle)

    state<State.Loading> {
        on<Event.OnLoadingEnded> {
            transitionTo(State.Idle, SideEffect.HideLoading)
        }
        on<Event.OnError> {
            transitionTo(State.Error, SideEffect.ShowError)
        }
    }

    state<State.Idle> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
        on<Event.OnError> {
            transitionTo(State.Error, SideEffect.ShowError)
        }
        on<Event.OnLoadingEnded> {
            transitionTo(State.Idle, SideEffect.HideLoading)
        }
    }

    state<State.Error> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
    }
}
