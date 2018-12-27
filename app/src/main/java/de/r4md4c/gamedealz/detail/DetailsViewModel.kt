package de.r4md4c.gamedealz.detail

import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.GetPlainDetails
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailsViewModel(
    private val navigator: Navigator,
    private val getPlainDetails: GetPlainDetails
) : AbstractViewModel() {

    fun onBuyClick(buyUrl: String) {
        navigator.navigateToUrl(buyUrl)
    }

    fun loadPlainDetails(plainId: String) = uiScope.launch(IO) {
        val details = getPlainDetails(TypeParameter(plainId))
        Timber.d("Found details for $details")
    }
}