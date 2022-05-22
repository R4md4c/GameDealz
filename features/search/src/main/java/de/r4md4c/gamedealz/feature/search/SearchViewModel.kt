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

package de.r4md4c.gamedealz.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class SearchViewModel @AssistedInject constructor(
    private val searchUseCase: SearchUseCase,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    data class State(
        val currentQuery: String = "",
        val searchResultsRequest: RequestState = RequestState.Loading,
    )

    private val searchArgs = SearchFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val refreshEvent = Channel<Unit>(Channel.UNLIMITED)

    private val query = savedStateHandle.getLiveData(KEY_QUERY, searchArgs.searchTerm).asFlow()

    private val queryAfterFiltering = query.debounce(DEBOUNCE_TIME).distinctUntilChanged()

    private val searchResultsFlow =
        merge(refreshEvent.receiveAsFlow().map { query.first() }, queryAfterFiltering)
            .filter(String::isNotBlank)
            .flatMapLatest(this::getSearchResultsFlow)

    val state: StateFlow<State> by lazy {
        combine(query, searchResultsFlow) { query, searchResults ->
            State(
                currentQuery = query,
                searchResultsRequest = searchResults,
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, State())
    }

    fun onSearchViewCollapse(navigator: Navigator) {
        navigator.navigateUp()
    }

    fun onQueryChanged(searchTerm: String) {
        savedStateHandle.set(KEY_QUERY, searchTerm)
    }

    fun onRefresh() {
        refreshEvent.trySend(Unit)
    }

    private fun getSearchResultsFlow(searchTerm: String) = flow {
        val usecaseResult = searchUseCase.invoke(searchTerm)

        val requestState = usecaseResult.fold(
            onSuccess = RequestState::Loaded,
            onFailure = { RequestState.Error(it.message.orEmpty()) }
        )

        emit(requestState)
    }.onStart {
        emit(RequestState.Loading)
    }

    sealed class RequestState {
        object Loading : RequestState()
        class Loaded(val searchResults: List<SearchResultModel>) : RequestState()
        class Error(val message: String) : RequestState()
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): SearchViewModel
    }

    private companion object {
        private const val KEY_QUERY = "query"
        private const val DEBOUNCE_TIME = 500L
    }
}
