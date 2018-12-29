package de.r4md4c.gamedealz.search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.VerticalLinearDecorator
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.detail.DetailsFragment
import de.r4md4c.gamedealz.search.SearchFragmentArgs.fromBundle
import kotlinx.android.synthetic.main.fragment_search.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment : BaseFragment() {

    private val searchTerm by lazy { fromBundle(arguments!!).searchTerm }

    private var listener: OnFragmentInteractionListener? = null

    private var searchView: SearchView? = null

    private val viewModel by viewModel<SearchViewModel>()

    private val navigator: Navigator by inject { parametersOf(requireActivity()) }

    private val resourcesProvider by inject<ResourcesProvider>()

    private val dateFormatter by inject<DateFormatter>()

    private var searchResultsLoaded = false

    private val searchAdapter by lazy {
        SearchAdapter(layoutInflater, resourcesProvider, dateFormatter) {
            it.prices.firstOrNull()?.let { priceModel ->
                listener?.onFragmentInteraction(DetailsFragment.toUri(it.title, it.gameId, priceModel.url))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_search)
        setupSearchMenuItem(toolbar.menu.findItem(R.id.search_bar))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        retry.setOnClickListener {
            searchView?.let { searchView ->
                viewModel.startSearch(searchView.query.toString())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!searchResultsLoaded) {
            viewModel.startSearch(searchTerm)
        }

        viewModel.searchResults.observe(this, Observer {
            emptyResultsTitleText.isVisible = it.isEmpty()
            searchAdapter.submitList(it)
            searchResultsLoaded = true
        })
        viewModel.sideEffects.observe(this, Observer {
            when (it) {
                is SideEffect.ShowLoading -> {
                    errorGroup.isVisible = false
                    progress.isVisible = true
                    recyclerView.isVisible = false
                    emptyResultsTitleText.isVisible = false
                }
                is SideEffect.HideLoading -> {
                    errorGroup.isVisible = false
                    progress.isVisible = false
                    recyclerView.isVisible = true
                    emptyResultsTitleText.isVisible = viewModel.searchResults.value?.isEmpty() == true
                    searchView?.clearFocus()
                }
                is SideEffect.ShowError -> {
                    errorText.text = it.error.localizedMessage
                    errorGroup.isVisible = true
                    progress.isVisible = false
                    emptyResultsTitleText.isVisible = false
                    recyclerView.isVisible = false
                }
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun setupRecyclerView() {
        with(recyclerView) {
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun toUri(searchTerm: String): Uri = DeepLinks.buildSearchUri(searchTerm)
    }
}
