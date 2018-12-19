package de.r4md4c.gamedealz

import androidx.paging.DataSource
import de.r4md4c.gamedealz.deals.DealsViewModel
import de.r4md4c.gamedealz.deals.datasource.DealsDataSourceFactory
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.home.HomeViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val MAIN = module {

    viewModel {
        HomeViewModel(get(), get(), get())
    }

    viewModel { DealsViewModel(get(), get()) }

    factory<DataSource.Factory<Int, DealModel>> { DealsDataSourceFactory(get()) }
}