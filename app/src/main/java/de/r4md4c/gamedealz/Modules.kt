package de.r4md4c.gamedealz

import androidx.paging.DataSource
import de.r4md4c.gamedealz.deals.DealsViewModel
import de.r4md4c.gamedealz.deals.datasource.DealsDataSourceFactory
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.home.HomeViewModel
import de.r4md4c.gamedealz.regions.RegionSelectionViewModel
import de.r4md4c.gamedealz.utils.state.StateMachineDelegate
import de.r4md4c.gamedealz.utils.state.UIStateMachineDelegate
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module

const val SCOPE_FRAGMENT = "fragment_scope"

val MAIN = module {

    scope<DataSource.Factory<Int, DealModel>>(SCOPE_FRAGMENT) { DealsDataSourceFactory(get(), get()) }

    scope<StateMachineDelegate>(SCOPE_FRAGMENT) {
        UIStateMachineDelegate()
    }

    scope(SCOPE_FRAGMENT) {
        DealsViewModel(get(), get(), get())
    }

    viewModel<HomeViewModel>()

    viewModel<RegionSelectionViewModel>()

}