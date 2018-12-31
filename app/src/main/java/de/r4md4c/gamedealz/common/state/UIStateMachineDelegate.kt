package de.r4md4c.gamedealz.common.state

import com.tinder.StateMachine
import timber.log.Timber
import java.lang.ref.WeakReference

class UIStateMachineDelegate : StateMachineDelegate {

    private var transitionBlock: WeakReference<((SideEffect) -> Unit)>? = null

    private val stateMachine by lazy {
        UI_STATE_MACHINE.with {
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

                Timber.d("Received a new transition: $validTransition")
                validTransition.sideEffect?.let { sideEffect -> transitionBlock?.get()?.invoke(sideEffect) }
            }
        }
    }

    override val state: State
        get() = stateMachine.state

    override fun transition(event: Event) {
        stateMachine.transition(event)
    }

    override fun onTransition(block: ((SideEffect) -> Unit)) {
        this.transitionBlock = WeakReference(block)
    }
}