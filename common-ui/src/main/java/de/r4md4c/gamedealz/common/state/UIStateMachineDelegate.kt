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
import timber.log.Timber

class UIStateMachineDelegate :
    StateMachineDelegate {

    private var transitionBlock: ((SideEffect) -> Unit)? = null

    private val stateMachine by lazy {
        UI_STATE_MACHINE.with {
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

                Timber.d("Received a new transition: $validTransition")
                validTransition.sideEffect?.let { sideEffect -> transitionBlock?.invoke(sideEffect) }
            }
        }
    }

    override val state: State
        get() = stateMachine.state

    override fun transition(event: Event) {
        stateMachine.transition(event)
    }

    override fun onTransition(block: ((SideEffect) -> Unit)) {
        this.transitionBlock = block
    }
}
