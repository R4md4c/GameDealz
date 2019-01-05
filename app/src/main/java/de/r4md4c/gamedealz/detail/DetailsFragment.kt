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

package de.r4md4c.gamedealz.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.detail.DetailsFragmentArgs.fromBundle
import de.r4md4c.gamedealz.detail.decorator.DetailsItemDecorator
import de.r4md4c.gamedealz.detail.item.*
import de.r4md4c.gamedealz.watchlist.AddToWatchListDialog
import kotlinx.android.synthetic.main.fragment_game_detail.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsFragment : BaseFragment() {

    private val priceModel by lazy { fromBundle(arguments!!).priceModel }

    private val title by lazy { fromBundle(arguments!!).title }

    private val plainId by lazy { fromBundle(arguments!!).plainId }

    private val buyUrl by lazy { fromBundle(arguments!!).buyUrl }

    private val detailsViewModel by viewModel<DetailsViewModel> { parametersOf(requireActivity()) }

    private val resourcesProvider: ResourcesProvider by inject()

    private val dateFormatter: DateFormatter by inject()

    private val gameDetailsAdapter by lazy { ItemAdapter<IItem<*, *>>() }
    private val pricesAdapter by lazy { ItemAdapter<IItem<*, *>>() }

    private val stateVisibilityHandler by inject<StateVisibilityHandler> {
        parametersOf(this, {
            detailsViewModel.loadPlainDetails(plainId)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(collapsing_toolbar, toolbar, findNavController())
        stateVisibilityHandler.onViewCreated()
        setupTitle()
        setupFab()
        content.apply {
            addItemDecoration(DetailsItemDecorator(context))
            layoutManager = LinearLayoutManager(context)
            adapter = FastAdapter.with<IItem<*, *>, ItemAdapter<IItem<*, *>>>(listOf(gameDetailsAdapter, pricesAdapter))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detailsViewModel.onSaveState()?.let {
            outState.putParcelable(STATE_DETAILS, it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            detailsViewModel.loadPlainDetails(plainId)
        } else {
            savedInstanceState.getParcelable<DetailsViewModelState>(STATE_DETAILS)
                ?.let { detailsViewModel.onRestoreState(it) }
        }

        detailsViewModel.sideEffect.observe(this, Observer {
            stateVisibilityHandler.onSideEffect(it)
        })

        detailsViewModel.gameInformation.observe(this, Observer {
            gameDetailsAdapter.add(
                HeaderItem(getString(R.string.about_game)),
                AboutGameItem(it.headerImage, it.shortDescription)
            )
        })

        detailsViewModel.screenshots.observe(this, Observer {
            gameDetailsAdapter.add(
                HeaderItem(getString(R.string.screenshots)),
                ScreenshotsSectionItems(it, resourcesProvider, content.recycledViewPool)
            )
        })

        detailsViewModel.prices.observe(this, Observer {
            renderPrices(it)
        })
    }

    private fun renderPrices(it: List<PriceDetails>) {
        viewScope.launch(dispatchers.Default) {
            withContext(dispatchers.Main) { progress.isVisible = true }
            val filterHeaderItem = FilterHeaderItem(
                getString(R.string.prices),
                R.menu.details_prices_sort_menu,
                detailsViewModel.currentFilterItemChoice
            ) { sortId -> handleFilterItemClick(sortId) }

            val desiredState = if (detailsViewModel.currentFilterItemChoice == R.id.menu_item_current_best) {
                R.id.state_current_best
            } else {
                R.id.state_historical_low
            }

            val pricesItems = listOf(filterHeaderItem) + it.map { priceDetails ->
                priceDetails.toPriceItem(
                    resourcesProvider,
                    dateFormatter,
                    desiredState,
                    detailsViewModel::onBuyButtonClick
                )
            }
            withContext(dispatchers.Main) {
                pricesAdapter.set(pricesItems)
                progress.isVisible = false
            }
        }
    }

    private fun setupFab() {
        addToWatchList.setOnClickListener {
            //            detailsViewModel.onBuyButtonClick(buyUrl)
            AddToWatchListDialog.newInstance(plainId, title, priceModel).show(childFragmentManager, null)
        }
    }

    private fun setupTitle() {
        collapsing_toolbar.title = title
    }

    private fun handleFilterItemClick(@IdRes clickedFilterItemId: Int) {
        detailsViewModel.onFilterChange(clickedFilterItemId)
    }

    companion object {
        @JvmStatic
        fun toUri(title: String, plainId: String, buyUrl: String): Uri =
            DeepLinks.buildDetailUri(plainId, title, buyUrl)

        private const val STATE_DETAILS = "state_detail"
    }
}
