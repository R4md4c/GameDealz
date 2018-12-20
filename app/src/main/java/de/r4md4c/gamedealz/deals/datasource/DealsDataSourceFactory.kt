package de.r4md4c.gamedealz.deals.datasource

import androidx.paging.DataSource
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.utils.state.Event
import de.r4md4c.gamedealz.utils.state.StateMachineDelegate

class DealsDataSourceFactory(
    private val getDealsUseCase: GetDealsUseCase,
    private val uiStateMachineDelegate: StateMachineDelegate<Event>
) : DataSource.Factory<Int, DealModel>() {

    override fun create(): DataSource<Int, DealModel> = DealsDataSourceImpl(getDealsUseCase, uiStateMachineDelegate)

}