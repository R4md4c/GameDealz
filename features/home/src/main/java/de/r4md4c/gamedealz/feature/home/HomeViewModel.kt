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

package de.r4md4c.gamedealz.feature.home

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.livedata.SingleLiveEvent
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class HomeViewModel @Inject internal constructor(
    private val dispatchers: IDispatchers,
    private val intentsProcessor: IntentProcessor<HomeMviViewEvent>,
    private val homeModelStore: ModelStore<HomeMviViewState>
) : ViewModel() {

    private val _closeDrawer by lazy { MutableLiveData<Unit>() }
    val closeDrawer: LiveData<Unit> by lazy { _closeDrawer }

    private val _onError by lazy { SingleLiveEvent<String>() }
    val onError: LiveData<String> by lazy { _onError }

    internal val modelState =
        homeModelStore.modelState().distinctUntilChanged()

    internal fun onViewEvents(lifecycle: CoroutineScope, viewEventFlow: Flow<HomeMviViewEvent>) {
        viewEventFlow.onEach { intentsProcessor.process(it) }.launchIn(lifecycle)
    }

    override fun onCleared() {
        super.onCleared()
        homeModelStore.cancel()
    }

    fun closeDrawer() {
        _closeDrawer.postValue(Unit)
    }

    fun onNavigateTo(navigator: Navigator, uri: String, extras: Parcelable? = null) {
        navigator.navigate(uri, extras)
    }
}
