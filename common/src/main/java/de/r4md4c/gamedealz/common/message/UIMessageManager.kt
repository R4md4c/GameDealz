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

package de.r4md4c.gamedealz.common.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class UIMessageManager<T : UIMessage> {

    private val mutex = Mutex()
    private val _messages = MutableStateFlow(emptyList<T>())

    val messages: Flow<T?> = _messages.map { it.firstOrNull() }.distinctUntilChanged()

    suspend fun emitUIMessage(uiMessage: T) = mutex.withLock {
        _messages.value = _messages.value + uiMessage
    }

    suspend fun clearMessage(id: Long) = mutex.withLock {
        _messages.value = _messages.value.filterNot { it.id == id }
    }
}

open class UIMessage(
    val id: Long = UUID.randomUUID().mostSignificantBits
)
