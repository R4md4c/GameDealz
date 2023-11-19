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

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.stfalcon.imageviewer.StfalconImageViewer
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.base.fragment.viewBinding
import de.r4md4c.gamedealz.common.di.ForActivity
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.common.navigation.Navigator
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.common.ui.databinding.LayoutErrorRetryEmptyBinding
import de.r4md4c.gamedealz.common.viewmodel.createAbstractSavedStateFactory
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentArgs.Companion.fromBundle
import de.r4md4c.gamedealz.feature.detail.databinding.FragmentGameDetailBinding
import de.r4md4c.gamedealz.feature.detail.decorator.DetailsFragmentItemDecorator
import de.r4md4c.gamedealz.feature.detail.di.DaggerDetailComponent
import de.r4md4c.gamedealz.feature.detail.item.AboutGameItem
import de.r4md4c.gamedealz.feature.detail.item.ExpandableScreenshotsHeader
import de.r4md4c.gamedealz.feature.detail.item.FilterHeaderItem
import de.r4md4c.gamedealz.feature.detail.item.HeaderItem
import de.r4md4c.gamedealz.feature.detail.item.ScreenshotItem
import de.r4md4c.gamedealz.feature.detail.item.toPriceItem
import de.r4md4c.gamedealz.feature.detail.model.PriceDetails
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("TooManyFunctions")
class DetailsFragment : BaseFragment(R.layout.fragment_game_detail) {

    private val title by lazy { fromBundle(requireArguments()).title }

    private val plainId by lazy { fromBundle(requireArguments()).plainId }

    @field:ForActivity
    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var viewNotifier: ViewNotifier

    @Inject
    lateinit var navigator: Navigator

    @Inject
    internal lateinit var detailsVMFactory: DetailsViewModel.Factory

    private val viewModel by viewModels<DetailsViewModel> {
        createAbstractSavedStateFactory(this, arguments) {
            detailsVMFactory.create(it)
        }
    }

    private val gameDetailsAdapter by lazy { ItemAdapter<IItem<*, *>>() }
    private val pricesAdapter by lazy { ItemAdapter<IItem<*, *>>() }
    private val mainAdapter by lazy {
        FastItemAdapter.with<IItem<*, *>, ItemAdapter<IItem<*, *>>>(
            listOf(
                gameDetailsAdapter,
                pricesAdapter
            )
        )
    }

    private val spanCount
        get() = resourcesProvider.getInteger(R.integer.screenshots_span_count)

    private val itemsDecorator by lazy {
        DetailsFragmentItemDecorator(requireContext())
    }

    private val binding by viewBinding(FragmentGameDetailBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGameDetailBinding.bind(view)
        val retryEmptyBinding = LayoutErrorRetryEmptyBinding.bind(view)

        NavigationUI.setupWithNavController(
            binding.collapsingToolbar,
            binding.toolbar,
            findNavController()
        )

        retryEmptyBinding.retry.setOnClickListener { viewModel.onRefresh() }
        setupTitle()
        setupFab()
        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    renderState(
                        retryEmptyBinding = retryEmptyBinding,
                        fragmentGameDetailBinding = binding,
                        state = state
                    )
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.content.apply {
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
        binding.addToWatchList.hide()
        binding.addToWatchList.setOnClickListener {
            onAddToWatchlistClick()
        }
    }

    private fun setupTitle() {
        binding.collapsingToolbar.title = title
    }

    private fun renderState(
        retryEmptyBinding: LayoutErrorRetryEmptyBinding,
        fragmentGameDetailBinding: FragmentGameDetailBinding,
        state: DetailsViewState
    ) = with(state) {

        if (loading) {
            fragmentGameDetailBinding.progress.isVisible = true
            retryEmptyBinding.errorGroup.isVisible = false
            fragmentGameDetailBinding.content.isVisible = false
        } else {
            fragmentGameDetailBinding.progress.isVisible = false
            retryEmptyBinding.errorGroup.isVisible = false
            fragmentGameDetailBinding.content.isVisible = true
        }

        if (!state.errorMessage.isNullOrEmpty()) {
            fragmentGameDetailBinding.progress.isVisible = false
            fragmentGameDetailBinding.content.isVisible = false
            retryEmptyBinding.errorGroup.isVisible = true
            retryEmptyBinding.errorText.text = state.errorMessage
        }

        renderSections(state.sections)

        renderAddToWatchlistButton(state)
    }

    private fun renderAddToWatchlistButton(state: DetailsViewState) {
        if (state.loading || state.errorMessage != null) {
            binding.addToWatchList.hide()
        } else {
            binding.addToWatchList.show()
        }

        binding.addToWatchList.setImageResource(
            if (state.isWatched) R.drawable.ic_added_to_watch_list
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
                        section.allScreenshots.size > spanCount,
                        section.isExpanded
                    ) { viewModel.onExpandClicked() }

                    gameDetailsAdapterItems += section.visibleScreenshots
                        .mapIndexed { index, aScreenshot ->
                            ScreenshotItem(aScreenshot, index) { position ->
                                onScreenShotClick(
                                    section.allScreenshots,
                                    position
                                )
                            }
                        }
                }
                is Section.PriceSection -> {
                    val filterHeaderItem = FilterHeaderItem(
                        getString(section.titleRes),
                        R.menu.details_prices_sort_menu,
                        section.currentSortOrder.toMenuIdRes()
                    ) { sortType ->
                        viewModel.onSortOrderChange(sortType)
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

    private fun askToRemove() {
        viewLifecycleOwner.lifecycleScope.launch {
            val yes = ask()
            if (yes) {
                viewModel.onRemoveFromWatchList()
            }
        }
    }

    private suspend fun ask() = suspendCoroutine<Boolean> { continuation ->
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.dialog_ask_remove_from_watch_list, title),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setPositiveButton(android.R.string.ok) { dialog, _ -> continuation.resume(true); dialog.dismiss() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> continuation.resume(false); dialog.dismiss() }
            .show()
    }

    private fun navigateToAddToWatchlistDialog(priceDetails: PriceDetails) {
        val directions = DetailsFragmentDirections.actionGamedetailsToAddToWatchlistDialog(
            title,
            plainId,
            priceDetails.priceModel
        )
        findNavController().navigate(directions)
    }

    private fun onScreenShotClick(screenshots: List<ScreenshotModel>, screenshotPosition: Int) {
        StfalconImageViewer.Builder(requireContext(), screenshots) { view, image ->
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

    private fun onAddToWatchlistClick() {
        val isWatched = viewModel.state.value.isWatched
        if (isWatched) {
            askToRemove()
        } else {
            val priceDetails =
                viewModel.state.value.sections.filterIsInstance<Section.PriceSection>()
                    .first()
                    .priceDetails.firstOrNull() ?: return
            navigateToAddToWatchlistDialog(
                priceDetails
            )
        }
    }

    override fun onInject(coreComponent: CoreComponent) {
        super.onInject(coreComponent)
        DaggerDetailComponent.factory()
            .create(requireActivity(), this, coreComponent)
            .inject(this)
    }
}
