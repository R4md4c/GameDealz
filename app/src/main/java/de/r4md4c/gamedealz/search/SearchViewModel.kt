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

package de.r4md4c.gamedealz.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(
    private val dispatchers: IDispatchers,
    private val searchUseCase: SearchUseCase,
    private val stateMachineDelegate: StateMachineDelegate
) : ViewModel() {

    private var currentJob: Job? = null

    private val queryChannel = viewModelScope.actor<String>(dispatchers.Default) {
        consumeAsFlow()
            .filter { it.isNotBlank() }
            .debounce(500)
            .distinctUntilChanged()
            .onCompletion { }
            .collect {
                currentJob?.cancelAndJoin()
                loadSearchResults(it)
            }
    }

    private val _searchResults by lazy { MutableLiveData<List<SearchResultModel>>() }
    val searchResults: LiveData<List<SearchResultModel>> by lazy { _searchResults }

    private val _stateMachineSignals by lazy { MutableLiveData<SideEffect>() }
    val sideEffects: LiveData<SideEffect> by lazy { _stateMachineSignals }

    init {
        stateMachineDelegate.onTransition { _stateMachineSignals.postValue(it) }
    }

    fun onSearchViewCollapse(navigator: Navigator) {
        navigator.navigateUp()
    }

    fun startSearch(searchTerm: String) {
        loadSearchResults(searchTerm)
    }

    fun onQueryChanged(searchTerm: String) {
        queryChannel.offer(searchTerm)
    }

    private fun loadSearchResults(searchTerm: String) {
        currentJob = viewModelScope.launch(dispatchers.IO) {
            stateMachineDelegate.transition(Event.OnLoadingStart)
            runCatching { searchUseCase(TypeParameter(searchTerm)) }
                .onSuccess {
                    _searchResults.postValue(it)
                    stateMachineDelegate.transition(Event.OnLoadingEnded)
                    if (it.isEmpty()) {
                        stateMachineDelegate.transition(Event.OnShowEmpty)
                    }
                }
                .onFailure {
                    Timber.e(it, "Exception happened while loading search results.")
                    stateMachineDelegate.transition(Event.OnError(it))
                    stateMachineDelegate.transition(Event.OnLoadingEnded)
                }
        }
    }
}
