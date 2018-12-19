package de.r4md4c.gamedealz.deals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.toLiveData
import de.r4md4c.gamedealz.BuildConfig
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.model.StoreModel
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.utils.debounce
import de.r4md4c.gamedealz.utils.viewmodel.AbstractViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

class DealsViewModel(
    private val factory: DataSource.Factory<Int, DealModel>,
    private val selectedStoresUseCase: GetSelectedStoresUseCase
) : AbstractViewModel() {

    private var channel: ReceiveChannel<List<StoreModel>>? = null

    val deals by lazy {
        factory.toLiveData(BuildConfig.DEFAULT_PAGE_SIZE)
    }

    val loading: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun init() {
        uiScope.launch(IO) {
            channel = selectedStoresUseCase()

            channel?.debounce(uiScope, 1500)?.consumeEach {
                Timber.d("Change")
                deals.value?.dataSource?.invalidate()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        channel?.cancel()
    }

}