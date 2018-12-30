package de.r4md4c.gamedealz

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.paging.DataSource
import de.r4md4c.gamedealz.common.navigator.AndroidNavigator
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.OnRetryClick
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.common.state.UIStateMachineDelegate
import de.r4md4c.gamedealz.deals.DealsViewModel
import de.r4md4c.gamedealz.deals.datasource.DealsDataSourceFactory
import de.r4md4c.gamedealz.detail.DetailsViewModel
import de.r4md4c.gamedealz.domain.model.DealModel
import de.r4md4c.gamedealz.home.HomeViewModel
import de.r4md4c.gamedealz.regions.RegionSelectionViewModel
import de.r4md4c.gamedealz.search.SearchViewModel
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.core.parameter.parametersOf
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

    viewModel<SearchViewModel>()

    viewModel<RegionSelectionViewModel>()

    viewModel { (activity: Activity) -> DetailsViewModel(get(parameters = { parametersOf(activity) }), get(), get()) }

    factory<Navigator> { (activity: Activity) ->
        AndroidNavigator(
            activity,
            activity.findNavController(R.id.nav_host_fragment)
        )
    }

    factory { (fragment: Fragment, onRetry: OnRetryClick) -> StateVisibilityHandler(fragment, onRetry) }

}