package de.r4md4c.gamedealz.common.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class AbstractViewModel : ViewModel() {

    private val viewModelJob = SupervisorJob()

    protected val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}