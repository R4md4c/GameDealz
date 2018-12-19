package de.r4md4c.gamedealz.home

import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.domain.usecase.ToggleStoresUseCase
import de.r4md4c.gamedealz.utils.GlobalExceptionHandler
import de.r4md4c.gamedealz.utils.viewmodel.AbstractViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val getCurrentActiveRegion: GetCurrentActiveRegionUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val toggleStoresUseCase: ToggleStoresUseCase
) : AbstractViewModel() {

    private var _storesChannel: ReceiveChannel<List<StoreModel>>? = null

    val currentRegion = MutableLiveData<Pair<String, String>>()

    val loading = MutableLiveData<Boolean>()

    val stores = MutableLiveData<List<StoreModel>>()

    fun init() = uiScope.launch(GlobalExceptionHandler("Failure during init()")) {
        loading.postValue(true)

        val activeRegion = getCurrentActiveRegion()

        currentRegion.postValue(activeRegion.regionCode to activeRegion.country.displayName())

        _storesChannel = getStoresUseCase(TypeParameter(activeRegion))
        withContext(IO) {
            _storesChannel?.consumeEach {
                stores.postValue(it)
            }
        }

        loading.postValue(false)
    }

    fun onStoreSelected(store: StoreModel) = uiScope.launch(GlobalExceptionHandler("Failed to select a store")) {
        toggleStoresUseCase(CollectionParameter(setOf(store)))
    }

    override fun onCleared() {
        super.onCleared()
        _storesChannel?.cancel()
    }

}