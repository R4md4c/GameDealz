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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.VerticalLinearDecorator
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.common.navigator.Navigator
import de.r4md4c.gamedealz.common.state.SideEffect
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

    private val searchAdapter by lazy { SearchAdapter(layoutInflater) }

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
                viewModel.onQueryChanged(searchView.query.toString())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.searchResults.observe(this, Observer {
            emptyResultsTitleText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            searchAdapter.submitList(it)
        })
        viewModel.sideEffects.observe(this, Observer {
            when (it) {
                is SideEffect.ShowLoading -> {
                    errorGroup.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    emptyResultsTitleText.visibility = View.GONE
                }
                is SideEffect.HideLoading -> {
                    errorGroup.visibility = View.GONE
                    progress.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    emptyResultsTitleText.visibility =
                            if (viewModel.searchResults.value?.isEmpty() == true) View.VISIBLE else View.GONE
                    searchView?.clearFocus()
                }
                is SideEffect.ShowError -> {
                    errorText.text = it.error.localizedMessage

                    errorGroup.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    emptyResultsTitleText.visibility = View.GONE
                    recyclerView.visibility = View.GONE
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

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.onQueryChanged(newText)
                    return true
                }
            })

            setQuery(searchTerm, false)
            viewModel.onQueryChanged(searchTerm)
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
