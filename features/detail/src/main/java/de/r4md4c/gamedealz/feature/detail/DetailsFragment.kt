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

package de.r4md4c.gamedealz.feature.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.stfalcon.imageviewer.StfalconImageViewer
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.di.viewmodel.ViewModelFactoryCreator
import de.r4md4c.commonproviders.di.viewmodel.components
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.deepllink.DeepLinks
import de.r4md4c.gamedealz.common.di.ForActivity
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.common.mvi.MviViewModel
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.state.SideEffect
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentArgs.Companion.fromBundle
import de.r4md4c.gamedealz.feature.detail.decorator.DetailsFragmentItemDecorator
import de.r4md4c.gamedealz.feature.detail.di.DaggerDetailComponent
import de.r4md4c.gamedealz.feature.detail.di.DaggerDetailsRetainedComponent
import de.r4md4c.gamedealz.feature.detail.item.AboutGameItem
import de.r4md4c.gamedealz.feature.detail.item.ExpandableScreenshotsHeader
import de.r4md4c.gamedealz.feature.detail.item.FilterHeaderItem
import de.r4md4c.gamedealz.feature.detail.item.HeaderItem
import de.r4md4c.gamedealz.feature.detail.item.ScreenshotItem
import de.r4md4c.gamedealz.feature.detail.item.toPriceItem
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import de.r4md4c.gamedealz.feature.detail.mvi.toMenuIdRes
import kotlinx.android.synthetic.main.fragment_game_detail.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@Suppress("TooManyFunctions")
class DetailsFragment : BaseFragment() {

    private val title by lazy { fromBundle(arguments!!).title }

    private val plainId by lazy { fromBundle(arguments!!).plainId }

    @Inject
    lateinit var viewModelFactoryCreator: ViewModelFactoryCreator

    @field:ForActivity
    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var viewNotifier: ViewNotifier

    @Inject
    lateinit var stateVisibilityHandler: StateVisibilityHandler

    @Inject
    lateinit var navigator: Navigator

    @Inject
    internal lateinit var detailsMviViewModel: MviViewModel<DetailsViewState, DetailsMviEvent>

    private val eventsChannel = Channel<DetailsMviEvent>()

    private val scopedComponent by components {
        DaggerDetailsRetainedComponent.factory()
            .create(
                fromBundle(requireArguments()),
                requireContext().coreComponent()
            )
    }

    /*private val detailsViewModel by viewModels<DetailsViewModel> {
        viewModelFactoryCreator.create(
            this
        )
    }*/

    private val gameDetailsAdapter by lazy { ItemAdapter<IItem<*, *>>() }
    private val pricesAdapter by lazy { ItemAdapter<IItem<*, *>>() }
    private val mainAdapter by lazy {
        FastItemAdapter.with<IItem<*, *>, ItemAdapter<IItem<*, *>>>(listOf(gameDetailsAdapter, pricesAdapter))
    }

    private val spanCount
        get() = resourcesProvider.getInteger(R.integer.screenshots_span_count)

    private val itemsDecorator by lazy {
        DetailsFragmentItemDecorator(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(collapsing_toolbar, toolbar, findNavController())
        stateVisibilityHandler.onViewCreated()
        setupTitle()
        setupFab()
        setupRecyclerView()

        detailsMviViewModel.onViewEvents(
            eventsChannel.consumeAsFlow(),
            viewLifecycleOwner.lifecycleScope
        )
        detailsMviViewModel.modelState
            .onEach { renderState(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupRecyclerView() {
        content.apply {
            addItemDecoration(itemsDecorator)
            layoutManager =
                GridLayoutManager(context, spanCount).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            mainAdapter.getItem(position) ?: return spanCount
                            return when (mainAdapter.getItemViewType(position)) {
                                R.layout.layout_screenshot_item -> 1
                                else -> spanCount
                            }
                        }
                    }.apply { isSpanIndexCacheEnabled = true }
                }
            adapter = mainAdapter
        }
    }

    private fun setupFab() {
        addToWatchList.hide()
        addToWatchList.setOnClickListener {
            /*if (detailsViewModel.isAddedToWatchList.value == true) {
                askToRemove()
            } else {
                detailsViewModel.prices.value?.firstOrNull()?.let { priceDetails ->
                    navigateToAddToWatchlistDialog(priceDetails)
                }
            }*/
        }
    }

    private fun setupTitle() {
        collapsing_toolbar.title = title
    }

    private fun renderState(state: DetailsViewState) = with(state) {
        if (loading) {
            stateVisibilityHandler.onSideEffect(SideEffect.ShowLoading)
        } else {
            stateVisibilityHandler.onSideEffect(SideEffect.HideLoading)
        }

        renderSections(state.sections)

        renderAddToWatchlistButton(isWatched)
    }

    private fun renderAddToWatchlistButton(isWatched: Boolean?) {
        if (isWatched == null) {
            addToWatchList.hide()
            return
        }

        addToWatchList.show()
        addToWatchList.setImageResource(
            if (isWatched) R.drawable.ic_added_to_watch_list
            else R.drawable.ic_add_to_watch_list
        )
    }

    private fun renderSections(sections: List<Section>) {
        val gameDetailsAdapterItems = mutableListOf<AbstractItem<*, *>>()
        val pricesAdapterItems = mutableListOf<AbstractItem<*, *>>()

        sections.forEach { section ->
            when (section) {
                is Section.GameInfoSection -> {
                    gameDetailsAdapterItems += HeaderItem(getString(section.titleRes))
                    gameDetailsAdapterItems += AboutGameItem(section.imageUrl, section.description)
                }
                is Section.ScreenshotSection -> {
                    gameDetailsAdapterItems += ExpandableScreenshotsHeader(
                        section.screenshots.size > spanCount
                    ) {
                        applyRestOfScreenshots(section, section.isExpanded).also {
                            isExpanded = !isExpanded
                        }
                    }
                    gameDetailsAdapterItems += section.screenshots
                        .take(spanCount)
                        .mapIndexed { index, aScreenshot ->
                            ScreenshotItem(aScreenshot, index)
                            { position -> onScreenShotClick(section.screenshots, position) }
                        }
                }
                is Section.PriceSection -> {
                    val filterHeaderItem = FilterHeaderItem(
                        getString(section.titleRes),
                        R.menu.details_prices_sort_menu,
                        section.currentSortOrder.toMenuIdRes()
                    ) { sortType ->
                        eventsChannel.offer(DetailsMviEvent.PriceFilterChangeEvent(sortType))
                    }

                    val prices = section.priceDetails.map { priceDetails ->
                        priceDetails.toPriceItem(
                            resourcesProvider,
                            dateFormatter,
                            section.currentSortOrder,
                            navigator::navigateToUrl
                        )
                    }
                    pricesAdapterItems += filterHeaderItem
                    pricesAdapterItems += prices
                }
            }.exhaustive
        }.takeIf { sections.isNotEmpty() }?.let {
            gameDetailsAdapter.setNewList(gameDetailsAdapterItems.toList())
            pricesAdapter.setNewList(pricesAdapterItems.toList())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        detailsViewModel.onSaveState()?.let {
//            outState.putParcelable(STATE_DETAILS, it)
//        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*if (savedInstanceState == null) {
            detailsViewModel.loadPlainDetails(plainId)
        } else {
            savedInstanceState.getParcelable<DetailsViewModelState>(STATE_DETAILS)
                ?.let { detailsViewModel.onRestoreState(it) }
        }

        detailsViewModel.loadIsAddedToWatchlist(plainId)

        detailsViewModel.isAddedToWatchList.observe(viewLifecycleOwner, Observer {
            addToWatchList.setImageResource(
                if (it) R.drawable.ic_added_to_watch_list
                else R.drawable.ic_add_to_watch_list
            )
        })

        detailsViewModel.sideEffect.observe(viewLifecycleOwner, Observer {
            stateVisibilityHandler.onSideEffect(it)
        })

        detailsViewModel.gameInformation.observe(viewLifecycleOwner, Observer {
            gameDetailsAdapter.add(
                HeaderItem(getString(R.string.about_game)),
                AboutGameItem(it.headerImage, it.shortDescription)
            )
        })

        detailsViewModel.screenshots.observe(viewLifecycleOwner, Observer { screenshots ->
            gameDetailsAdapter.add(
                ExpandableScreenshotsHeader(
                    screenshots.size > spanCount
                ) {
                    applyRestOfScreenshots(isExpanded).also { isExpanded = !isExpanded }
                }
            )
            gameDetailsAdapter.add(screenshots.take(spanCount)
                .mapIndexed { index, aScreenshot -> ScreenshotItem(aScreenshot, index, this::onScreenShotClick) })
        })

        detailsViewModel.prices.observe(viewLifecycleOwner, Observer {
            renderPrices(it)
        })*/
    }

    /*private fun renderPrices(it: List<PriceDetails>) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            progress.isVisible = true
            val pricesItems = withContext(dispatchers.Default) {
                val filterHeaderItem = FilterHeaderItem(
                    getString(R.string.prices),
                    R.menu.details_prices_sort_menu,
                    detailsViewModel.currentFilterItemChoice
                ) { sortId -> handleFilterItemClick(sortId) }

                val desiredState =
                    if (detailsViewModel.currentFilterItemChoice == R.id.menu_item_current_best) {
                        R.id.state_current_best
                    } else {
                        R.id.state_historical_low
                    }

                listOf(filterHeaderItem) + it.map { priceDetails ->
                    priceDetails.toPriceItem(
                        resourcesProvider,
                        dateFormatter,
                        desiredState,
                        navigator::navigateToUrl
                    )
                }
            }

            pricesAdapter.set(pricesItems)
            progress.isVisible = false
            addToWatchList.show()
        }
    }



    private fun navigateToAddToWatchlistDialog(priceDetails: PriceDetails) {
        val directions = DetailsFragmentDirections.actionGamedetailsToAddToWatchlistDialog(
            title,
            plainId,
            priceDetails.priceModel
        )
        findNavController().navigate(directions)
    }

    private fun handleFilterItemClick(@IdRes clickedFilterItemId: Int) {
        detailsViewModel.onFilterChange(clickedFilterItemId)
    }

    private fun askToRemove() =
        viewLifecycleOwner.lifecycleScope.launchWithCatching(dispatchers.Main, {
            val yes = ask()
            if (yes) {
                val isRemoved = detailsViewModel.removeFromWatchlist(plainId)
                if (isRemoved) {
                    viewNotifier.notify(getString(R.string.watchlist_remove_successfully, title))
                }
            }
        }) {
            Timber.e(it, "Failed to remove $plainId from the Watchlist")
        }

    private suspend fun ask() = suspendCoroutine<Boolean> { continuation ->
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.dialog_ask_remove_from_watch_list, title),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setPositiveButton(android.R.string.yes) { dialog, _ -> continuation.resume(true); dialog.dismiss() }
            .setNegativeButton(android.R.string.no) { dialog, _ -> continuation.resume(false); dialog.dismiss() }
            .show()
    }

    private fun applyRestOfScreenshots(remove: Boolean) {
        val allScreenshots = detailsViewModel.screenshots.value ?: return
        val restScreenshots = detailsViewModel.getRestOfScreenshots().takeIf { it.isNotEmpty() } ?: return
        val thirdScreenshot = allScreenshots.getOrNull(spanCount - 1) ?: return

        val lastScreenshotPivot = gameDetailsAdapter.getAdapterPosition(thirdScreenshot.hashCode().toLong())

        if (remove) {
            gameDetailsAdapter.removeRange(lastScreenshotPivot + 1, restScreenshots.size)
        } else {
            gameDetailsAdapter.add(restScreenshots.mapIndexed { index, item ->
                ScreenshotItem(
                    item,
                    index + spanCount, this::onScreenShotClick
                )
            })
        }
    }

    private fun onScreenShotClick(screenshotPosition: Int) {
        val screenshots = detailsViewModel.screenshots.value ?: return
        StfalconImageViewer.Builder<ScreenshotModel>(requireContext(), screenshots) { view, image ->
            val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
                strokeWidth = resourcesProvider.getDimension(R.dimen.progress_stroke_size)
                centerRadius = resourcesProvider.getDimension(R.dimen.progress_size)
                setColorSchemeColors(requireContext().resolveThemeColor(R.attr.colorSecondary))
                start()
            }
            GlideApp.with(view)
                .load(image.full)
                .placeholder(circularProgressDrawable)
                .into(view)
        }.also {
            it.withStartPosition(screenshotPosition)
                .show()
        }
    }

    */

    private fun onScreenShotClick(screenshots: List<ScreenshotModel>, screenshotPosition: Int) {
        StfalconImageViewer.Builder<ScreenshotModel>(requireContext(), screenshots) { view, image ->
            val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
                strokeWidth = resourcesProvider.getDimension(R.dimen.progress_stroke_size)
                centerRadius = resourcesProvider.getDimension(R.dimen.progress_size)
                setColorSchemeColors(requireContext().resolveThemeColor(R.attr.colorSecondary))
                start()
            }
            GlideApp.with(view)
                .load(image.full)
                .placeholder(circularProgressDrawable)
                .into(view)
        }.also {
            it.withStartPosition(screenshotPosition)
                .show()
        }
    }

    private fun applyRestOfScreenshots(
        screenshotsSection: Section.ScreenshotSection,
        remove: Boolean
    ) {
        val allScreenshots = screenshotsSection.screenshots
        val restScreenshots = screenshotsSection.restOfScreenshots(resourcesProvider)
        val thirdScreenshot = allScreenshots.getOrNull(spanCount - 1) ?: return

        val lastScreenshotPivot =
            gameDetailsAdapter.getAdapterPosition(thirdScreenshot.hashCode().toLong())

        if (remove) {
            gameDetailsAdapter.removeRange(lastScreenshotPivot + 1, restScreenshots.size)
        } else {
            gameDetailsAdapter.add(restScreenshots.mapIndexed { index, item ->
                ScreenshotItem(
                    item,
                    index + spanCount
                ) { position -> onScreenShotClick(screenshotsSection.screenshots, position) }
            })
        }
    }

    override fun onInject(coreComponent: CoreComponent) {
        super.onInject(coreComponent)
        DaggerDetailComponent.factory()
            .create(requireActivity(), this, scopedComponent, coreComponent)
            .inject(this)
    }

    companion object {
        @JvmStatic
        fun toUri(title: String, plainId: String, buyUrl: String): Uri =
            DeepLinks.buildDetailUri(plainId, title, buyUrl)

        private const val STATE_DETAILS = "state_detail"
    }
}
