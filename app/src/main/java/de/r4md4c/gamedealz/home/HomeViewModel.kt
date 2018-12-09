package de.r4md4c.gamedealz.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.usecase.GetRegionsUseCase
import de.r4md4c.gamedealz.utils.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeViewModel(private val getRegionsUseCase: GetRegionsUseCase) : ViewModel() {

    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun retrieveRegions() = uiScope.launch(GlobalExceptionHandler("Failed to load regions")) {
        val useCaseRegions = getRegionsUseCase.regions()

        regions.postValue(useCaseRegions)
    }

    val regions = MutableLiveData<List<RegionWithCountries>>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}