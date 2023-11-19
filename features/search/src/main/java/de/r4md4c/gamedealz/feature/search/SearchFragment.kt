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

package de.r4md4c.gamedealz.feature.search

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
import androidx.recyclerview.widget.LinearLayoutManager
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.base.fragment.viewBinding
import de.r4md4c.gamedealz.common.decorator.VerticalLinearDecorator
import de.r4md4c.gamedealz.common.di.ForActivity
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.ui.databinding.LayoutErrorRetryEmptyBinding
import de.r4md4c.gamedealz.common.viewmodel.createAbstractSavedStateFactory
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.feature.search.SearchFragmentArgs.Companion.fromBundle
import de.r4md4c.gamedealz.feature.search.databinding.FragmentSearchBinding
import de.r4md4c.gamedealz.feature.search.di.DaggerSearchComponent
import de.r4md4c.gamedealz.feature.search.model.SearchItemRenderModel
import de.r4md4c.gamedealz.feature.search.model.toRenderModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFragment : BaseFragment() {

    private val searchTerm by lazy { fromBundle(requireArguments()).searchTerm }

    private var searchView: SearchView? = null

    @Inject
    lateinit var viewModelFactory: SearchViewModel.Factory

    @Inject
    lateinit var navigator: Navigator

    @field:ForActivity
    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var dateFormatter: DateFormatter

    private val binding by viewBinding(FragmentSearchBinding::bind)

    private val viewModel by viewModels<SearchViewModel> {
        createAbstractSavedStateFactory(this, arguments) {
            viewModelFactory.create(it)
        }
    }

    private val searchAdapter by lazy {
        SearchAdapter(layoutInflater) { renderModel ->
            renderModel.currentBestPriceModel?.let { priceModel ->
                navigateToGameDetails(renderModel, priceModel)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_search)
        setupSearchMenuItem(toolbar.menu.findItem(R.id.search_bar))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val retryEmptyBinding = LayoutErrorRetryEmptyBinding.bind(view)
        retryEmptyBinding.retry.setOnClickListener { viewModel.onRefresh() }

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    renderState(state, retryEmptyBinding)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.content) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(VerticalLinearDecorator(context))
            adapter = searchAdapter
        }
    }

    private fun setupSearchMenuItem(searchMenuItem: MenuItem) = with(searchMenuItem) {
        expandActionView()

        setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return false
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.onSearchViewCollapse(navigator)
                return true
            }
        })

        (actionView as? SearchView)?.run {
            searchView = this
            setQuery(searchTerm, false)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.onQueryChanged(newText)
                    return true
                }
            })
        }
    }

    override fun onInject(coreComponent: CoreComponent) {
        super.onInject(coreComponent)
        DaggerSearchComponent.factory()
            .create(requireActivity(), this, coreComponent)
            .inject(this)
    }

    private fun renderState(
        state: SearchViewModel.State,
        retryEmptyBinding: LayoutErrorRetryEmptyBinding
    ) {
        val errorState =
            state.searchResultsRequest as? SearchViewModel.RequestState.Error
        val loadedState =
            state.searchResultsRequest as? SearchViewModel.RequestState.Loaded
        val loadingState =
            state.searchResultsRequest as? SearchViewModel.RequestState.Loading

        retryEmptyBinding.errorGroup.isVisible = errorState != null
        retryEmptyBinding.errorText.text = errorState?.message

        retryEmptyBinding.emptyGroup.isVisible =
            loadedState?.searchResults?.isEmpty() ?: false
        binding.progress.isVisible = loadingState != null
        binding.content.isVisible = loadedState != null

        loadedState?.let { loaded ->
            searchAdapter.submitList(
                loaded.searchResults.map {
                    it.toRenderModel(
                        resourcesProvider,
                        dateFormatter
                    )
                }
            )
        }
    }

    private fun navigateToGameDetails(
        renderModel: SearchItemRenderModel,
        priceModel: PriceModel
    ) {
        val directions = SearchFragmentDirections.actionSearchFragmentToGameDetailFragment(
            renderModel.gameId,
            renderModel.title.toString(),
            priceModel.url
        )
        findNavController().navigate(directions)
    }
}
