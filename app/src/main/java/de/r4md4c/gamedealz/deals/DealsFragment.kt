package de.r4md4c.gamedealz.deals

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.fragment_deals.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class DealsFragment : Fragment(), LifecycleOwner {
    private var listener: OnFragmentInteractionListener? = null

    private val dealsViewModel by viewModel<DealsViewModel>()

    private val adapter by lazy { DealsAdapter() }

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
        dealsViewModel.loading.observe(this, Observer {
            progress.visibility = if (it) View.VISIBLE else View.GONE
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
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DealsFragment()
    }
}
