package de.r4md4c.gamedealz.utils.state

import com.tinder.StateMachine

class UIStateMachineDelegate : StateMachineDelegate<Event> {

    private var transitionBlock: ((SideEffect) -> Unit)? = null

    private val stateMachine by lazy {
        UI_STATE_MACHINE.with {
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

                validTransition.sideEffect?.let { sideEffect -> transitionBlock?.invoke(sideEffect) }
            }
        }
    }

    override fun transition(event: Event) {
        stateMachine.transition(event)
    }

    override fun onTransition(block: ((SideEffect) -> Unit)?) {
        this.transitionBlock = block
    }
}