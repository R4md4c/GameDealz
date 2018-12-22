package de.r4md4c.gamedealz.utils.state

import com.tinder.StateMachine

sealed class State {
    object Loading : State()
    object Idle : State()
    object LoadingMore : State()
    object Error : State()
}

sealed class Event {
    object OnLoadingStart : Event()
    object OnLoadingMoreStarted : Event()
    object OnLoadingMoreEnded : Event()
    data class OnError(val msgError: String?) : Event()
    object OnLoadingEnded : Event()
}

sealed class SideEffect {
    object ShowLoading : SideEffect()
    object HideLoading : SideEffect()
    object ShowLoadingMore : SideEffect()
    object HideLoadingMore : SideEffect()
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

    state<State.LoadingMore> {
        on<Event.OnLoadingMoreEnded> {
            transitionTo(State.Idle, SideEffect.HideLoadingMore)
        }
        on<Event.OnLoadingMoreStarted> {
            transitionTo(State.Loading, SideEffect.ShowLoadingMore)
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
        on<Event.OnLoadingMoreStarted> {
            transitionTo(State.LoadingMore, SideEffect.ShowLoadingMore)
        }
        on<Event.OnLoadingMoreEnded> {
            transitionTo(State.Idle, SideEffect.HideLoadingMore)
        }
    }

    state<State.Error> {
        on<Event.OnLoadingStart> {
            transitionTo(State.Loading, SideEffect.ShowLoading)
        }
    }
}
