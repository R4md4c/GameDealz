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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.commonproviders.di.viewmodel.ViewModelFactoryCreator
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.base.fragment.viewBinding
import de.r4md4c.gamedealz.common.decorator.StaggeredGridDecorator
import de.r4md4c.gamedealz.common.ui.databinding.LayoutErrorRetryEmptyBinding
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.feature.deals.databinding.FragmentDealsBinding
import de.r4md4c.gamedealz.feature.deals.di.DaggerDealsComponent
import de.r4md4c.gamedealz.feature.deals.filter.DealsFilterDialogFragment
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentDirections
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class DealsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactoryCreator

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

    private val binding by viewBinding(FragmentDealsBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutErrorBinding = LayoutErrorRetryEmptyBinding.bind(view)

        val context = requireContext()
        drawerLayout?.let {
            NavigationUI.setupWithNavController(
                binding.toolbar,
                findNavController(),
                drawerLayout
            )
        }
        setupRecyclerView()
        setupFilterFab()
        layoutErrorBinding.retry.setOnClickListener { dealsAdapter.retry() }

        binding.swipeToRefresh.setColorSchemeColors(context.resolveThemeColor(R.attr.colorSecondary))
        binding.swipeToRefresh.setProgressBackgroundColorSchemeColor(
            context.resolveThemeColor(R.attr.swipe_refresh_background)
        )
        binding.swipeToRefresh.setOnRefreshListener {
            dealsAdapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dealsAdapter.loadStateFlow.onEach { loadStates ->
                    renderState(binding, layoutErrorBinding, loadStates)
                }.launchIn(this)

                dealsViewModel.pager.collectLatest { pagingData ->
                    dealsAdapter.submitData(
                        pagingData
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        super.onCreateOptionsMenu(toolbar)
        toolbar.inflateMenu(R.menu.menu_deals)
        val searchMenuItem = toolbar.menu.findItem(R.id.menu_search)
        (searchMenuItem.actionView as? SearchView)?.setOnQueryTextListener(OnQueryTextListener(searchMenuItem))
    }

    private fun setupRecyclerView() = with(binding.content) {
        adapter = dealsAdapter.withLoadStateFooter(DealsFooterLoadStateAdapter())
        addItemDecoration(StaggeredGridDecorator(requireContext()))
        layoutManager =
            StaggeredGridLayoutManager(resources.getInteger(R.integer.deals_span_count), VERTICAL)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.filterFab.hide() else binding.filterFab.show()
            }
        })
    }

    private fun setupFilterFab() {
        binding.filterFab.setOnClickListener {
            DealsFilterDialogFragment().show(
                childFragmentManager,
                null
            )
        }
    }

    override fun onInject(coreComponent: CoreComponent) {
        super.onInject(coreComponent)
        DaggerDealsComponent.factory()
            .create(this, coreComponent)
            .inject(this)
    }

    private fun renderState(
        binding: FragmentDealsBinding,
        retryBinding: LayoutErrorRetryEmptyBinding,
        value: CombinedLoadStates
    ) {
        binding.swipeToRefresh.isRefreshing = value.refresh is LoadState.Loading

        val error = value.refresh as? LoadState.Error
        retryBinding.errorGroup.isVisible = error != null
        retryBinding.errorText.text = error?.error?.localizedMessage
    }

    private inner class OnQueryTextListener(private val searchMenuItem: MenuItem) :
        SearchView.OnQueryTextListener {

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
