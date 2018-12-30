package de.r4md4c.gamedealz.deals

import androidx.lifecycle.MutableLiveData
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.toLiveData
import de.r4md4c.gamedealz.BuildConfig
import de.r4md4c.gamedealz.common.debounce
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.viewmodel.AbstractViewModel
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.drop
import kotlinx.coroutines.launch

class DealsViewModel(
    private val factory: DataSource.Factory<Int, DealModel>,
    private val selectedStoresUseCase: GetSelectedStoresUseCase,
    private val uiStateMachineDelegate: StateMachineDelegate
) : AbstractViewModel() {

    val deals by lazy {
        factory.toLiveData(
            Config(
                pageSize = BuildConfig.DEFAULT_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSizeHint = BuildConfig.DEFAULT_PAGE_SIZE * 2
            )
        )
    }

    val sideEffect: MutableLiveData<SideEffect> by lazy {
        MutableLiveData<SideEffect>()
    }

    fun init() {
        uiScope.launch(IO) {
            selectedStoresUseCase().debounce(uiScope, 500).drop(1).consumeEach {
                deals.value?.dataSource?.invalidate()
            }
        }

        uiStateMachineDelegate.onTransition {
            sideEffect.postValue(it)
        }
        deals.value?.dataSource?.invalidate()
    }

    fun onRefresh() {
        deals.value?.dataSource?.invalidate()
    }

    override fun onCleared() {
        super.onCleared()
        uiStateMachineDelegate.onTransition(null)
    }

}