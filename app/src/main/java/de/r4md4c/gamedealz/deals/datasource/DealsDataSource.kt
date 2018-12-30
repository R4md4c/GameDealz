package de.r4md4c.gamedealz.deals.datasource

import androidx.paging.PositionalDataSource
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.State
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class DealsDataSource(
    private val getDealsUseCase: GetDealsUseCase,
    private val stateMachineDelegate: StateMachineDelegate
) : PositionalDataSource<DealModel>() {

    private var job: Job? = null
    private val scope = CoroutineScope(IO)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<DealModel>) {
        if (params.startPosition == 0 || stateMachineDelegate.state is State.LoadingMore) {
            return
        }
        job?.cancel()
        job = scope.launch {
            runCatching {
                stateMachineDelegate.transition(Event.OnLoadingMoreStarted)
                getDealsUseCase(PageParameter(params.startPosition + 1, params.loadSize)).apply {
                    check(isActive)
                }
            }.onSuccess {
                stateMachineDelegate.transition(Event.OnLoadingMoreEnded)
                if (!it.second.isEmpty()) {
                    callback.onResult(it.second)
                }
            }.onFailure {
                stateMachineDelegate.transition(Event.OnError(it))
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
            stateMachineDelegate.transition(Event.OnLoadingStart)

            runCatching {
                getDealsUseCase(PageParameter(params.requestedStartPosition, params.pageSize)).apply {
                    check(isActive)
                }
            }.onSuccess { deals ->
                stateMachineDelegate.transition(Event.OnLoadingEnded)
                if (deals.second.isEmpty()) {
                    stateMachineDelegate.transition(Event.OnShowEmpty)
                }
                callback.onResult(deals.second, deals.second.size)
            }.onFailure {

                stateMachineDelegate.transition(Event.OnError(it))
                Timber.e(it, "Failed to get initial deals")
            }
        }
    }
}