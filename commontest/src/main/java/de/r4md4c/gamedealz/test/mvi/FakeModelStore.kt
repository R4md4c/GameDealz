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

import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviState
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first

class FakeModelStore<S : MviState>(initialState: S) : ModelStore<S> {
    private val conflatedBroadcastChannel = ConflatedBroadcastChannel(initialState)

    override suspend fun process(intent: Intent<S>) {
        conflatedBroadcastChannel.send(intent.reduce(conflatedBroadcastChannel.value))
    }

    override fun modelState(): Flow<S> = conflatedBroadcastChannel.asFlow()

    suspend fun lastValue() = modelState().first()
}
