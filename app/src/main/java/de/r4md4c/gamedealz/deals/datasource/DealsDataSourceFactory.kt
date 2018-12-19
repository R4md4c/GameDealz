package de.r4md4c.gamedealz.deals.datasource

import androidx.paging.DataSource
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase

class DealsDataSourceFactory(private val getDealsUseCase: GetDealsUseCase) : DataSource.Factory<Int, DealModel>() {

    override fun create(): DataSource<Int, DealModel> = DealsDataSourceImpl(getDealsUseCase)

}