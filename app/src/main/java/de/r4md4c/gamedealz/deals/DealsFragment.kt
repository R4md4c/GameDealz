package de.r4md4c.gamedealz.deals

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.StaggeredGridDecorator
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.detail.DetailsFragment
import de.r4md4c.gamedealz.search.SearchFragment
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class DealsFragment : BaseFragment() {

    private var listener: OnFragmentInteractionListener? = null

    private val dealsViewModel by inject<DealsViewModel> { parametersOf(requireActivity()) }

    private val adapter by lazy {
        DealsAdapter {
            listener?.onFragmentInteraction(DetailsFragment.toUri(it.title, it.gameId, it.urls.buyUrl))
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
        NavigationUI.setupWithNavController(toolbar, findNavController(), drawerLayout)
        setupRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealsViewModel.deals.observe(this, Observer {
            adapter.submitList(it)
        })
        dealsViewModel.sideEffect.observe(this, Observer {
            when (it) {
                is SideEffect.ShowLoading -> progress.isVisible = true
                is SideEffect.HideLoading -> progress.isVisible = false
                is SideEffect.ShowLoadingMore -> adapter.showProgress(true)
                is SideEffect.HideLoadingMore -> adapter.showProgress(false)
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

    override fun onCreateOptionsMenu(toolbar: Toolbar) {
        super.onCreateOptionsMenu(toolbar)
        toolbar.inflateMenu(R.menu.menu_deals)
        (toolbar.menu.findItem(R.id.menu_search).actionView as? SearchView)?.let { searchView ->
            searchView.setOnQueryTextListener(OnQueryTextListener(searchView))
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        context?.let { recyclerView.addItemDecoration(StaggeredGridDecorator(it)) }
        recyclerView.layoutManager =
                StaggeredGridLayoutManager(resources.getInteger(R.integer.deals_span_count), VERTICAL)
    }

    private inner class OnQueryTextListener(private val searchView: SearchView) : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            listener?.onFragmentInteraction(SearchFragment.toUri(query))
            searchView.clearFocus()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean = false

    }
}
