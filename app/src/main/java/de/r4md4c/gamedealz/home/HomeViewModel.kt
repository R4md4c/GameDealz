package de.r4md4c.gamedealz.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegion
import de.r4md4c.gamedealz.utils.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeViewModel(private val getCurrentActiveRegion: GetCurrentActiveRegion) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun retrieveRegions() = uiScope.launch(GlobalExceptionHandler("Failed to load regions")) {
        val activeRegion = getCurrentActiveRegion()

        currentRegion.postValue(activeRegion)
    }

    val currentRegion = MutableLiveData<ActiveRegion>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}