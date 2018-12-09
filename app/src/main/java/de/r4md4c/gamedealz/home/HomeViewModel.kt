package de.r4md4c.gamedealz.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.domain.model.displayName
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegion
import de.r4md4c.gamedealz.domain.usecase.GetStoresUseCase
import de.r4md4c.gamedealz.utils.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentActiveRegion: GetCurrentActiveRegion,
    private val getStoresUseCase: GetStoresUseCase
) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val currentRegion = MutableLiveData<Pair<String, String>>()

    val loading = MutableLiveData<Boolean>()

    val stores = MutableLiveData<List<Store>>()

    fun retrieveRegions() = uiScope.launch(GlobalExceptionHandler("Failed to load regions")) {
        loading.postValue(true)
        val activeRegion = getCurrentActiveRegion()

        currentRegion.postValue(activeRegion.region.regionCode to activeRegion.country.displayName())

        val retrievedStores = getStoresUseCase(activeRegion)
        loading.postValue(false)
        stores.postValue(retrievedStores)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}