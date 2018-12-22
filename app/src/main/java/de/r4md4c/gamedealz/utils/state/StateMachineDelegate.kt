package de.r4md4c.gamedealz.utils.state

/**
 * A delegate for the interaction with the state machine.
 */
interface StateMachineDelegate<T : Event> {

    /**
     * Transition to a state via an event.
     *
     * @param event the event that causes the transition.
     */
    fun transition(event: T)

    /**
     * The current state of the machine.
     */
    val state: State

    /**
     * Callback when transition changes.
     */
    fun onTransition(block: ((SideEffect) -> Unit)?)
}
