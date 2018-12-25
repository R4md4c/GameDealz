package de.r4md4c.gamedealz.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.common.debounce
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.SearchResultModel
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val stateMachineDelegate: StateMachineDelegate
) : AbstractViewModel() {

    private var currentJob: Job? = null

    private val queryChannel = uiScope.actor<String>(Dispatchers.Default) {
        filter { it.isNotBlank() }
            .debounce(uiScope, 500)
            .consumeEach {
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

    fun onQueryChanged(searchTerm: String) {
        queryChannel.offer(searchTerm)
    }

    private fun loadSearchResults(searchTerm: String) {
        currentJob = uiScope.launch(IO) {
            stateMachineDelegate.transition(Event.OnLoadingStart)
            runCatching { searchUseCase(TypeParameter(searchTerm)) }
                .onSuccess {
                    _searchResults.postValue(it)
                    stateMachineDelegate.transition(Event.OnLoadingEnded)
                }
                .onFailure {
                    Timber.e(it, "Exception happened while loading search results.")
                    stateMachineDelegate.transition(Event.OnError(it))
                    stateMachineDelegate.transition(Event.OnLoadingEnded)
                }
        }
    }
}
