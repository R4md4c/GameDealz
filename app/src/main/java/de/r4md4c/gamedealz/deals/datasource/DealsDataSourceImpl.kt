package de.r4md4c.gamedealz.deals.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class DealsDataSourceImpl(private val getDealsUseCase: GetDealsUseCase) : PositionalDataSource<DealModel>(),
    DealsDataSource {

    private val _loading = MutableLiveData<Boolean>()
    override val loading: LiveData<Boolean> = _loading

    private var job: Job? = null
    private val scope = CoroutineScope(IO)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<DealModel>) {
        job?.cancel()
        job = scope.launch {
            getDealsUseCase.runCatching {
                invoke(PageParameter(params.startPosition + 1, params.loadSize))
            }.onSuccess {
                if (!it.second.isEmpty()) {
                    callback.onResult(it.second)
                }
            }.onFailure {
                Timber.e(
                    it,
                    "Failed to deals in loadRange method startPosition: ${params.startPosition + 1}, pageSize: ${params.loadSize}"
                )
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<DealModel>) {
        job?.cancel()
        job = scope.launch {
            _loading.postValue(true)

            getDealsUseCase.runCatching {
                invoke(PageParameter(params.requestedStartPosition, params.pageSize))
            }.onSuccess { deals ->
                if (deals.second.isEmpty()) {
                    callback.onResult(emptyList(), 0, 0)
                } else {
                    callback.onResult(deals.second, deals.second.size, deals.first)
                }
            }.onFailure {
                Timber.e(it, "Failed to get initial deals")
            }

            _loading.postValue(false)
        }
    }
}