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

package de.r4md4c.gamedealz.feature.deals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.commonproviders.di.viewmodel.ViewModelFactoryCreator
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.StaggeredGridDecorator
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.feature.deals.di.DaggerDealsComponent
import de.r4md4c.gamedealz.feature.deals.filter.DealsFilterDialogFragment
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentDirections
import kotlinx.android.synthetic.main.fragment_deals.*
import javax.inject.Inject

class DealsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactoryCreator

    @Inject
    lateinit var stateVisibilityHandler: StateVisibilityHandler

    private val dealsViewModel by viewModels<DealsViewModel> { viewModelFactory.create(this) }

    private val dealsAdapter by lazy {
        DealsAdapter {
            findNavController().navigate(
                DetailsFragmentDirections.actionGlobalGameDetailFragment(
                    plainId = it.gameId,
                    title = it.title.toString(),
                    buyUrl = it.buyUrl
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dealsViewModel.init()

        val context = requireContext()
        toolbar?.let { NavigationUI.setupWithNavController(it, findNavController(), drawerLayout) }
        setupRecyclerView()
        setupFilterFab()
        stateVisibilityHandler.onViewCreated()
        stateVisibilityHandler.onRetryClick = { dealsViewModel.onRefresh() }

        swipeToRefresh.setColorSchemeColors(context.resolveThemeColor(R.attr.colorSecondary))
        swipeToRefresh.setProgressBackgroundColorSchemeColor(
            context.resolveThemeColor(R.attr.swipe_refresh_background)
        )
        swipeToRefresh.setOnRefreshListener { dealsViewModel.onRefresh() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealsViewModel.deals.observe(viewLifecycleOwner, Observer {
            dealsAdapter.submitList(it)
        })
        dealsViewModel.sideEffect.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SideEffect.ShowLoadingMore -> dealsAdapter.showProgress(true)
                is SideEffect.HideLoadingMore -> dealsAdapter.showProgress(false)
                is SideEffect.ShowLoading, SideEffect.HideLoading, SideEffect.ShowEmpty -> {
                    if (it is SideEffect.ShowLoading) {
                        filterFab.hide()
                    } else {
                        filterFab.show()
                    }
                    stateVisibilityHandler.onSideEffect(it)
                }
                else -> stateVisibilityHandler.onSideEffect(it)
            }
        })
    }

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        super.onCreateOptionsMenu(toolbar)
        toolbar.inflateMenu(R.menu.menu_deals)
        val searchMenuItem = toolbar.menu.findItem(R.id.menu_search)
        (searchMenuItem.actionView as? SearchView)?.setOnQueryTextListener(OnQueryTextListener(searchMenuItem))
    }

    private fun setupRecyclerView() = with(content) {
        adapter = dealsAdapter
        addItemDecoration(StaggeredGridDecorator(requireContext()))
        layoutManager =
                StaggeredGridLayoutManager(resources.getInteger(R.integer.deals_span_count), VERTICAL)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) filterFab?.hide() else filterFab?.show()
            }
        })
    }

    private fun setupFilterFab() {
        filterFab.setOnClickListener { DealsFilterDialogFragment().show(childFragmentManager, null) }
    }

    override fun onInject(coreComponent: CoreComponent) {
        super.onInject(coreComponent)
        DaggerDealsComponent.factory()
            .create(this, coreComponent)
            .inject(this)
    }

    private inner class OnQueryTextListener(private val searchMenuItem: MenuItem) : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            findNavController().navigate(
                DealsFragmentDirections.actionDealsFragmentToSearchFragment(query)
            )
            searchMenuItem.collapseActionView()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean = false
    }
}
