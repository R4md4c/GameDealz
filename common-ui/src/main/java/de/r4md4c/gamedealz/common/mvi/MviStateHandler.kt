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

package de.r4md4c.gamedealz.common.mvi

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import de.r4md4c.gamedealz.common.aware.LifecycleAware
import de.r4md4c.gamedealz.common.aware.SavedStateAware
import de.r4md4c.gamedealz.common.di.ViewModelScope
import de.r4md4c.gamedealz.common.unsafeLazy
import javax.inject.Inject

@ViewModelScope
class MviStateHandler<S> @Inject constructor(
    private val modelStore: ModelStore<S>
) : LifecycleAware, SavedStateAware<S>
        where S : Parcelable,
              S : MviState {

    private var savedStateRegisterOwner: SavedStateRegistryOwner? = null

    override fun createStateRestorer(): StateRestorer<S> =
        StateRestorer(
            unsafeLazy {
                val stateRegistry = savedStateRegisterOwner!!.savedStateRegistry
                stateRegistry.consumeRestoredStateForKey(PROVIDER_KEY)?.getParcelable(STATE_KEY) as? S
            }
        )

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        require(owner is SavedStateRegistryOwner) {
            """
            The lifecycle owner doesn't implement  SavedStateRegistryOwner.
            Are you sure that this lifecycle aware is added to the Fragment's Lifecycle owner rather
            than its view? 
            """.trimIndent()
        }

        savedStateRegisterOwner = owner
        savedStateRegisterOwner?.savedStateRegistry?.registerSavedStateProvider(PROVIDER_KEY) {
            bundleOf().apply {
                (modelStore.currentState as? Parcelable)?.let {
                    putParcelable(STATE_KEY, it)
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        savedStateRegisterOwner?.savedStateRegistry?.unregisterSavedStateProvider(PROVIDER_KEY)
        savedStateRegisterOwner = null
        owner.lifecycle.removeObserver(this)
    }

    private companion object {
        private const val STATE_KEY = "mvi:state_key"
        private const val PROVIDER_KEY = "mvi:provider_key"
    }
}
