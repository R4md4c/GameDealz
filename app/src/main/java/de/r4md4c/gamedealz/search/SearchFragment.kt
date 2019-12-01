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

package de.r4md4c.gamedealz.search

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
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.r4md4c.commonproviders.FOR_ACTIVITY
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.VerticalLinearDecorator
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.feature.detail.DetailsFragment
import de.r4md4c.gamedealz.search.SearchFragmentArgs.Companion.fromBundle
import de.r4md4c.gamedealz.search.model.toRenderModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.IllegalStateException

class SearchFragment : BaseFragment() {

    private val searchTerm by lazy { fromBundle(arguments!!).searchTerm }

    private var listener: OnFragmentInteractionListener? = null

    private var searchView: SearchView? = null

    private val viewModel by viewModel<SearchViewModel>()

    private val navigator: Navigator by inject { parametersOf(requireActivity()) }

    private val resourcesProvider by inject<ResourcesProvider>(name = FOR_ACTIVITY) { parametersOf(requireActivity()) }

    private val dateFormatter by inject<DateFormatter>()

    private var searchResultsLoaded = false

    private val searchAdapter by lazy {
        SearchAdapter(layoutInflater) {
            it.currentBestPriceModel?.let { priceModel ->
                listener?.onFragmentInteraction(
                    DetailsFragment.toUri(it.title.toString(), it.gameId, priceModel.url), null
                )
            }
        }
    }

    private val stateVisibilityHandler by inject<StateVisibilityHandler> {
        parametersOf(this, {
            searchView?.let { searchView ->
                viewModel.startSearch(searchView.query.toString())
            }
        })
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
        setupRecyclerView()
        stateVisibilityHandler.onViewCreated()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let { searchResultsLoaded = it.getBoolean(EXTRA_ALREADY_LOADED, false) }

        if (!searchResultsLoaded) {
            viewModel.startSearch(searchTerm)
        }

        viewModel.searchResults.observe(this, Observer {
            viewLifecycleOwner.lifecycleScope.launch {
                progress.isVisible = true
                withContext(dispatchers.Default) {
                    it.map { searchResult -> searchResult.toRenderModel(resourcesProvider, dateFormatter) }
                }.also { renderModels ->
                    searchAdapter.submitList(renderModels)
                    searchResultsLoaded = true
                    searchView?.clearFocus()
                    progress.isVisible = false
                }
            }
        })
        viewModel.sideEffects.observe(this, Observer {
            stateVisibilityHandler.onSideEffect(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (activity?.isChangingConfigurations == true) {
            outState.putBoolean(EXTRA_ALREADY_LOADED, searchResultsLoaded)
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw IllegalStateException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun setupRecyclerView() {
        with(content) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(VerticalLinearDecorator(context))
            adapter = searchAdapter
        }
    }

    private fun setupSearchMenuItem(searchMenuItem: MenuItem) = with(searchMenuItem) {
        expandActionView()

        setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return false
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri, extras: Parcelable?)
    }

    companion object {
        @JvmStatic
        fun toUri(searchTerm: String): Uri = DeepLinks.buildSearchUri(searchTerm)

        private const val EXTRA_ALREADY_LOADED = "alread_loaded"
    }
}
