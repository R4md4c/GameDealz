package de.r4md4c.gamedealz.search

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.SearchUseCase
import de.r4md4c.gamedealz.utils.navigator.Navigator
import de.r4md4c.gamedealz.utils.viewmodel.AbstractViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(private val searchUseCase: SearchUseCase) : AbstractViewModel() {

    fun onSearchViewCollapse(navigator: Navigator) {
        navigator.navigateUp()
    }

    fun onSubmitQuery(searchTerm: String) = uiScope.launch {
        if (searchTerm.isBlank()) {
            return@launch
        }

        val searchResults = searchUseCase(TypeParameter(searchTerm.trim()))
        Timber.d("Search results: $searchResults")
    }
}