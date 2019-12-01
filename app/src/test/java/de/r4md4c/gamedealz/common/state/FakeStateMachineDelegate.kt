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

import kotlin.properties.Delegates

class FakeStateMachineDelegate(private val initialState: State = State.Idle) :
    StateMachineDelegate {

    var lastEvent: Event by Delegates.notNull()

    override fun transition(event: Event) {
        this.lastEvent = event
    }

    override val state: State
        get() = initialState

    override fun onTransition(block: (SideEffect) -> Unit) {
    }
}
