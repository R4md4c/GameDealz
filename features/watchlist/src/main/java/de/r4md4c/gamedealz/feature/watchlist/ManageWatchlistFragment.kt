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

package de.r4md4c.gamedealz.feature.watchlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.UndoHelper
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback
import de.r4md4c.commonproviders.di.viewmodel.ViewModelFactoryCreator
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.decorator.VerticalLinearDecorator
import de.r4md4c.gamedealz.common.di.ForActivity
import de.r4md4c.gamedealz.common.launchWithCatching
import de.r4md4c.gamedealz.common.state.StateVisibilityHandler
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.domain.model.ManageWatchlistModel
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentDirections
import de.r4md4c.gamedealz.feature.watchlist.di.DaggerWatchlistComponent
import de.r4md4c.gamedealz.feature.watchlist.item.ManageWatchlistItem
import de.r4md4c.gamedealz.feature.watchlist.item.toManageWatchlistItem
import de.r4md4c.gamedealz.feature.watchlist.shortcut.ShortcutManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("TooManyFunctions")
class ManageWatchlistFragment : BaseFragment(), SimpleSwipeCallback.ItemSwipeCallback {

    private val itemsAdapter by lazy { FastItemAdapter<ManageWatchlistItem>() }

    @Inject
    lateinit var viewModelFactory: ViewModelFactoryCreator

    @field:ForActivity
    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    @Inject
    lateinit var stateVisibilityHandler: StateVisibilityHandler

    @Inject
    lateinit var shortcutManager: ShortcutManager

    private val watchlistViewModel: ManageWatchlistViewModel by viewModels {
        viewModelFactory.create(
            this
        )
    }

    private val swipeToRefresh: SwipeRefreshLayout
        get() = requireView().findViewById(R.id.swipeToRefresh) as SwipeRefreshLayout

    private val toolbar: Toolbar
        get() = requireView().findViewById(R.id.toolbar) as Toolbar

    private val content: RecyclerView
        get() = requireView().findViewById(R.id.content) as RecyclerView

    private val emptyResultsTitleText: TextView
        get() = requireView().findViewById(R.id.emptyResultsTitleText) as TextView

    private val undoHelper by lazy {
        UndoHelper<ManageWatchlistItem>(itemsAdapter, UndoListener())
    }

    private val swipeCallback by lazy {
        val bgColor = requireActivity().resolveThemeColor(R.attr.colorPrimary)
        val colorOnPrimary = requireActivity().resolveThemeColor(R.attr.colorOnPrimary)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_white_24dp)!!
        DrawableCompat.setTint(drawable, colorOnPrimary)
        SimpleSwipeCallback(this, drawable, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            .withBackgroundSwipeLeft(bgColor)
            .withBackgroundSwipeRight(bgColor)
            .withLeaveBehindSwipeRight(drawable)
            .withLeaveBehindSwipeLeft(drawable)
    }

    private val itemTouchHelper by lazy { ItemTouchHelper(swipeCallback) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_manage_watchlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateVisibilityHandler.onRetryClick = { watchlistViewModel.onSwipeToRefresh() }
        stateVisibilityHandler.onViewCreated()
        emptyResultsTitleText.setText(R.string.watchlist_empty)
        swipeToRefresh.setColorSchemeColors(requireContext().resolveThemeColor(R.attr.colorSecondary))
        swipeToRefresh.setOnRefreshListener { watchlistViewModel.onSwipeToRefresh() }
        setRecyclerView()
        setToolbar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        watchlistViewModel.init()

        watchlistViewModel.sideEffects.observe(viewLifecycleOwner, Observer {
            stateVisibilityHandler.onSideEffect(it)
        })
        watchlistViewModel.watchlistLiveData.observe(
            viewLifecycleOwner,
            Observer { renderModels(it) })
        watchlistViewModel.lastCheckDate.observe(viewLifecycleOwner, Observer {
            toolbar.subtitle = resourcesProvider.getString(R.string.manage_watch_list_last_checked, it)
        })
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launchWithCatching(
            dispatchers.Default,
            { watchlistViewModel.refreshWatchlist() }) {
            Timber.e(it, "Failed to refresh Watchlist")
        }
    }

    @SuppressLint("Range")
    override fun itemSwiped(position: Int, direction: Int) {
        view?.let {
            val item = (itemsAdapter.getAdapterItem(position).tag as? ManageWatchlistModel) ?: return
            watchlistViewModel.onItemSwiped(item)
            undoHelper.remove(
                it,
                resourcesProvider.getString(R.string.watchlist_removed_watchee, item.watcheeModel.title),
                resourcesProvider.getString(R.string.action_undo),
                Snackbar.LENGTH_LONG,
                setOf(position)
            ).addCallbackWhenDismissedViaAction { watchlistViewModel.onItemUndone(item) }
        }
    }

    private fun renderModels(modelsList: List<ManageWatchlistModel>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val transformedList = withContext(dispatchers.Default) {
                modelsList.mapNotNull { model ->
                    model.toManageWatchlistItem(resourcesProvider)?.withTag(model)
                }
            }
            itemsAdapter.set(transformedList)
        }
    }

    private fun setRecyclerView() {
        itemTouchHelper.attachToRecyclerView(content)
        content.adapter = itemsAdapter
        content.addItemDecoration(
            VerticalLinearDecorator(
                requireContext()
            )
        )
        itemsAdapter.withOnClickListener { _, _, item, _ ->
            val model = item.tag as? ManageWatchlistModel ?: return@withOnClickListener false
            val direction = DetailsFragmentDirections
                .actionGlobalGameDetailFragment(model.watcheeModel.plainId, model.watcheeModel.title, "")
            findNavController().navigate(direction)
            watchlistViewModel.markAsRead(model)
            true
        }
    }

    private fun setToolbar() {
        toolbar.inflateMenu(R.menu.menu_manage_watch_list)
        toolbar.setupWithNavController(findNavController(), drawerLayout)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_add_shortcut -> {
                    askUser()
                    true
                }
                else -> false
            }
        }
    }

    private inline fun Snackbar.addCallbackWhenDismissedViaAction(crossinline block: () -> Unit) {
        addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_ACTION) {
                    block()
                }
            }
        })
    }

    private fun askUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            showDialog().takeIf { it }?.let {
                shortcutManager.addManageWatchlistShortcut()
            }
        }
    }

    private suspend fun showDialog(): Boolean = suspendCoroutine { continuation ->
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.watchlist_shortcut_dialog_message)
            .setPositiveButton(android.R.string.yes) { dialog, _ -> dialog.dismiss(); continuation.resume(true) }
            .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss(); continuation.resume(false) }
            .show()
    }

    override fun onInject(coreComponent: CoreComponent) {
        DaggerWatchlistComponent.factory()
            .create(requireActivity(), this, coreComponent)
            .inject(this)
    }

    private inner class UndoListener : UndoHelper.UndoListener<ManageWatchlistItem> {
        override fun commitRemove(
            positions: MutableSet<Int>,
            removed: ArrayList<FastAdapter.RelativeInfo<ManageWatchlistItem>>
        ) {
            watchlistViewModel.onRemoveWatchee(removed.mapNotNull { it.item.tag as? ManageWatchlistModel })
        }
    }
}
