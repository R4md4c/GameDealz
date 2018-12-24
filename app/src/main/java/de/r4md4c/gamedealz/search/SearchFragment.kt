package de.r4md4c.gamedealz.search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.utils.deepllink.DeepLinks


const val ARG_SEARCH_TERM = "search_term"

class SearchFragment : Fragment() {

    private val searchTerm by lazy { arguments?.getString(ARG_SEARCH_TERM) }
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        menu.findItem(R.id.search_bar).run {
            expandActionView()
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return false
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    findNavController().navigateUp()
                    return true
                }
            })
        }

        (menu.findItem(R.id.search_bar).actionView as? SearchView)?.setQuery(searchTerm, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
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

    companion object {
        @JvmStatic
        fun toUri(searchTerm: String): Uri = DeepLinks.buildSearchUri(searchTerm)
    }
}
