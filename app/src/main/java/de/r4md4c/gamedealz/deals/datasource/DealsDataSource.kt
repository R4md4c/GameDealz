/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.deals.datasource

import androidx.paging.PositionalDataSource
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.common.state.Event
import de.r4md4c.gamedealz.common.state.State
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.deals.model.DealRenderModel
import de.r4md4c.gamedealz.deals.model.toRenderModel
import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class DealsDataSource(
    private val getDealsUseCase: GetDealsUseCase,
    private val stateMachineDelegate: StateMachineDelegate,
    private val resourcesProvider: ResourcesProvider,
    dispatchers: IDispatchers
) : PositionalDataSource<DealRenderModel>() {

    private var job: Job? = null
    private val scope = CoroutineScope(dispatchers.IO)

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<DealRenderModel>) {
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
                    callback.onResult(it.second.map { d ->
                        d.toRenderModel(
                            resourcesProvider,
                            R.color.newPriceColor,
                            R.color.oldPriceColor
                        )
                    })
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

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<DealRenderModel>) {
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
                callback.onResult(
                    deals.second.map {
                        it.toRenderModel(
                            resourcesProvider,
                            R.color.newPriceColor,
                            R.color.oldPriceColor
                        )
                    },
                    deals.second.size
                )
            }.onFailure {

                stateMachineDelegate.transition(Event.OnError(it))
                Timber.e(it, "Failed to get initial deals")
            }
        }
    }
}