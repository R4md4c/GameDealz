package de.r4md4c.gamedealz.deals

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.GridDecorator
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.detail.GameDetailFragment
import de.r4md4c.gamedealz.search.SearchFragment
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class DealsFragment : BaseFragment() {

    private var listener: OnFragmentInteractionListener? = null

    private val dealsViewModel by inject<DealsViewModel> { parametersOf("activity" to requireActivity()) }

    private val adapter by lazy {
        DealsAdapter {
            listener?.onFragmentInteraction(GameDetailFragment.newInstance(it.title, it.gameId))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        dealsViewModel.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealsViewModel.deals.observe(this, Observer {
            adapter.submitList(it)
        })
        dealsViewModel.sideEffect.observe(this, Observer {
            when (it) {
                is SideEffect.ShowLoading -> progress.visibility = VISIBLE
                is SideEffect.HideLoading -> progress.visibility = GONE
                is SideEffect.ShowLoadingMore -> adapter.showProgress(true)
                is SideEffect.HideLoadingMore -> adapter.showProgress(false)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_deals, menu)

        (menu.findItem(R.id.menu_search).actionView as? SearchView)?.let { searchView ->
            searchView.setOnQueryTextListener(OnQueryTextListener(searchView))
        }
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        context?.let { recyclerView.addItemDecoration(GridDecorator(it)) }
        recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.span_count), VERTICAL)
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
