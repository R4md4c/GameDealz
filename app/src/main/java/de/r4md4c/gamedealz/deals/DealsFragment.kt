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

package de.r4md4c.gamedealz.deals

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.StaggeredGridDecorator
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.deals.filter.DealsFilterDialogFragment
import de.r4md4c.gamedealz.detail.DetailsFragment
import de.r4md4c.gamedealz.search.SearchFragment
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class DealsFragment : BaseFragment() {

    private var listener: OnFragmentInteractionListener? = null

    private val dealsViewModel by viewModel<DealsViewModel> { parametersOf(requireActivity()) }

    private val stateVisibilityHandler by inject<StateVisibilityHandler> {
        parametersOf(this, { dealsViewModel.onRefresh() })
    }

    private val dealsAdapter by lazy {
        DealsAdapter {
            listener?.onFragmentInteraction(
                DetailsFragment.toUri(it.title.toString(), it.gameId, it.buyUrl), null
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dealsViewModel.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        NavigationUI.setupWithNavController(toolbar, findNavController(), drawerLayout)
        setupRecyclerView()
        setupFilterFab()
        stateVisibilityHandler.onViewCreated()
        swipeToRefresh.setColorSchemeColors(context.resolveThemeColor(R.attr.colorSecondary))
        swipeToRefresh.setProgressBackgroundColorSchemeColor(
            context.resolveThemeColor(R.attr.swipe_refresh_background)
        )
        swipeToRefresh.setOnRefreshListener { dealsViewModel.onRefresh() }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealsViewModel.deals.observe(this, Observer {
            dealsAdapter.submitList(it)
        })
        dealsViewModel.sideEffect.observe(this, Observer {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        super.onCreateOptionsMenu(toolbar)
        toolbar.inflateMenu(R.menu.menu_deals)
        val searchMenuItem = toolbar.menu.findItem(R.id.menu_search)
        (searchMenuItem.actionView as? SearchView)?.setOnQueryTextListener(OnQueryTextListener(searchMenuItem))
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri, extras: Parcelable?)
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

    private inner class OnQueryTextListener(private val searchMenuItem: MenuItem) : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            listener?.onFragmentInteraction(SearchFragment.toUri(query), null)
            searchMenuItem.collapseActionView()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean = false

    }
}
