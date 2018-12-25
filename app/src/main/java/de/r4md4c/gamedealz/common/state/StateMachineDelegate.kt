package de.r4md4c.gamedealz.common.state

/**
 * A delegate for the interaction with the state machine.
 */
interface StateMachineDelegate {

    /**
     * Transition to a state via an event.
     *
     * @param event the event that causes the transition.
     */
    fun transition(event: Event)

    /**
     * The current state of the machine.
     */
    val state: State

    /**
     * Callback when transition changes.
     */
    fun onTransition(block: ((SideEffect) -> Unit)?)
}
