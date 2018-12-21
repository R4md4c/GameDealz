package de.r4md4c.gamedealz.deals

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.SCOPE_FRAGMENT
import de.r4md4c.gamedealz.utils.decorator.GridDecorator
import de.r4md4c.gamedealz.utils.state.SideEffect
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope


class DealsFragment : Fragment(), LifecycleOwner {

    private var listener: OnFragmentInteractionListener? = null

    private val dealsViewModel by inject<DealsViewModel>()

    private val adapter by lazy { DealsAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(SCOPE_FRAGMENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dealsViewModel.init()
        dealsViewModel.deals.observe(this, Observer {
            adapter.submitList(it)
        })
        dealsViewModel.sideEffect.observe(this, Observer {
            when (it) {
                is SideEffect.ShowLoading -> progress.visibility = VISIBLE
                is SideEffect.HideLoading -> progress.visibility = GONE
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        context?.let { recyclerView.addItemDecoration(GridDecorator(it)) }
        recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.span_count), VERTICAL)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DealsFragment()
    }
}
