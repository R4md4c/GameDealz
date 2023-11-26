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

import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.common.mvi.MviState
import de.r4md4c.gamedealz.common.mvi.ReducibleMviResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class FakeModelStore<S : MviState>(initialState: S) : ModelStore<S> {
    private val conflatedBroadcastChannel = MutableStateFlow(initialState)

    override suspend fun process(result: MviResult<S>) {
        if (result is ReducibleMviResult<S>) {
            conflatedBroadcastChannel.value = result.reduce(conflatedBroadcastChannel.value)
        }
    }

    override fun modelState(): Flow<S> = conflatedBroadcastChannel.asStateFlow()

    override val currentState: S
        get() = conflatedBroadcastChannel.value

    suspend fun lastValue() = modelState().first()

    override fun dispose() = Unit
}
