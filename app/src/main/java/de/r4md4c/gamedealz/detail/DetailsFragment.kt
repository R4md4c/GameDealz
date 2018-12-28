package de.r4md4c.gamedealz.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.detail.DetailsFragmentArgs.fromBundle
import de.r4md4c.gamedealz.detail.decorator.DetailsItemDecorator
import de.r4md4c.gamedealz.detail.item.AboutGameItem
import de.r4md4c.gamedealz.detail.item.HeaderItem
import de.r4md4c.gamedealz.detail.item.PriceItem
import de.r4md4c.gamedealz.detail.item.ScreenshotsSectionItems
import kotlinx.android.synthetic.main.fragment_game_detail.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsFragment : BaseFragment() {

    private val title by lazy { fromBundle(arguments!!).title }

    private val plainId by lazy { fromBundle(arguments!!).plainId }

    private val buyUrl by lazy { fromBundle(arguments!!).buyUrl }

    private val detailsViewModel by viewModel<DetailsViewModel> { parametersOf(requireActivity()) }

    private val resourcesProvider: ResourcesProvider by inject()

    private val dateFormatter: DateFormatter by inject()

    private val itemsAdapter by lazy { FastItemAdapter<IItem<*, *>>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(collapsing_toolbar, toolbar, findNavController())
        setupTitle()
        setupFab()
        recyclerView.apply {
            addItemDecoration(DetailsItemDecorator(context))
            layoutManager = LinearLayoutManager(context)
            adapter = itemsAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            detailsViewModel.loadPlainDetails(plainId)
        }

        detailsViewModel.gameInformation.observe(this, Observer {
            itemsAdapter.add(
                HeaderItem(getString(R.string.about_game)),
                AboutGameItem(it.headerImage, it.shortDescription)
            )
        })

        detailsViewModel.screenshots.observe(this, Observer {
            itemsAdapter.add(
                HeaderItem(getString(R.string.screenshots)),
                ScreenshotsSectionItems(it, resourcesProvider, recyclerView.recycledViewPool)
            )
        })

        detailsViewModel.prices.observe(this, Observer {
            itemsAdapter.add(listOf(HeaderItem(getString(R.string.prices))) + it.map { priceDetails ->
                PriceItem(priceDetails, resourcesProvider, dateFormatter) { clickedDetails ->
                    detailsViewModel.onBuyButtonClick(clickedDetails.priceModel.url)
                }
            })
        })
    }

    private fun setupFab() {
        buyFab.setOnClickListener {
            detailsViewModel.onBuyButtonClick(buyUrl)
        }
    }

    private fun setupTitle() {
        collapsing_toolbar.title = title
    }


    companion object {
        @JvmStatic
        fun toUri(title: String, plainId: String, buyUrl: String): Uri =
            DeepLinks.buildDetailUri(plainId, title, buyUrl)
    }
}
