package de.r4md4c.gamedealz.search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.utils.deepllink.DeepLinks
import de.r4md4c.gamedealz.utils.navigator.Navigator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


const val ARG_SEARCH_TERM = "search_term"

class SearchFragment : Fragment() {

    private val searchTerm by lazy { arguments?.getString(ARG_SEARCH_TERM) }
    private var listener: OnFragmentInteractionListener? = null

    private val viewModel by viewModel<SearchViewModel>()

    private val navigator: Navigator by inject { parametersOf(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        with(menu.findItem(R.id.search_bar)) {
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


                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        viewModel.onSubmitQuery(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })

                setQuery(searchTerm, true)
            }
        }
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
