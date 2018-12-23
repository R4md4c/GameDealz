package de.r4md4c.gamedealz.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.r4md4c.gamedealz.domain.CollectionParameter
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.model.StoreModel
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

    private val _currentRegion by lazy { MutableLiveData<ActiveRegion>() }
    val currentRegion: LiveData<ActiveRegion> by lazy { _currentRegion }

    private val _regionsLoading by lazy { MutableLiveData<Boolean>() }
    val regionsLoading: LiveData<Boolean> by lazy { _regionsLoading }

    private val _stores by lazy { MutableLiveData<List<StoreModel>>() }
    val stores: LiveData<List<StoreModel>> by lazy { _stores }

    private val _openRegionSelectionDialog by lazy { MutableLiveData<ActiveRegion>() }
    val openRegionSelectionDialog: LiveData<ActiveRegion> by lazy { _openRegionSelectionDialog }

    fun init() = uiScope.launch(GlobalExceptionHandler("Failure during init()")) {
        _regionsLoading.postValue(true)

        val activeRegion = getCurrentActiveRegion()

        _currentRegion.postValue(activeRegion)

        _storesChannel = getStoresUseCase(TypeParameter(activeRegion))
        withContext(IO) {
            _storesChannel?.consumeEach {
                _stores.postValue(it)
            }
        }
    }

    fun onStoreSelected(store: StoreModel) = uiScope.launch(GlobalExceptionHandler("Failed to select a store")) {
        toggleStoresUseCase(CollectionParameter(setOf(store)))
    }

    fun onRegionChangeClicked() {
        uiScope.launch(IO) {
            val activeRegion = getCurrentActiveRegion()
            _openRegionSelectionDialog.postValue(activeRegion)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _storesChannel?.cancel()
    }

}