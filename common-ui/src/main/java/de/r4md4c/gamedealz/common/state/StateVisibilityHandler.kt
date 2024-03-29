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

package de.r4md4c.gamedealz.common.state

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.r4md4c.gamedealz.common.ui.R
import javax.inject.Inject

typealias OnRetryClick = () -> Unit

/**
 * Handles visibility of the widgets depending on the state machine's SideEffect.
 */
class StateVisibilityHandler @Inject constructor(
    private val fragment: Fragment
) {

    private val content: View?
        get() = fragment.view?.findViewById(R.id.content)

    private val progress: View?
        get() = fragment.view?.findViewById(R.id.progress)

    private val swipeToRefresh: SwipeRefreshLayout?
        get() = fragment.view?.findViewById(R.id.swipeToRefresh) as? SwipeRefreshLayout

    private val retryView: View?
        get() = fragment.view?.findViewById(R.id.retry)

    private val emptyGroup: View?
        get() = fragment.view?.findViewById(R.id.emptyGroup)

    private val errorGroup: View?
        get() = fragment.view?.findViewById(R.id.errorGroup)

    private val errorText: TextView?
        get() = fragment.view?.findViewById(R.id.errorText)

    var onRetryClick: OnRetryClick? = null

    fun onViewCreated() {
        retryView?.setOnClickListener { onRetryClick?.invoke() }
    }

    fun onSideEffect(sideEffect: SideEffect) {
        when (sideEffect) {
            is SideEffect.ShowLoading -> {
                progress?.isVisible = true
                swipeToRefresh?.isRefreshing = true
                emptyGroup?.isVisible = false
                errorGroup?.isVisible = false
                content?.isVisible = false
            }
            is SideEffect.HideLoading -> {
                progress?.isVisible = false
                swipeToRefresh?.isRefreshing = false
                errorGroup?.isVisible = false
                content?.isVisible = true
            }
            is SideEffect.ShowError -> {
                progress?.isVisible = false
                swipeToRefresh?.isRefreshing = false
                errorGroup?.isVisible = true
                errorText?.text = sideEffect.error.localizedMessage
                emptyGroup?.isVisible = false
                content?.isVisible = false
            }
            is SideEffect.ShowEmpty -> {
                progress?.isVisible = false
                swipeToRefresh?.isRefreshing = false
                errorGroup?.isVisible = false
                emptyGroup?.isVisible = true
                content?.isVisible = false
            }

            is SideEffect.ShowContent -> {
                progress?.isVisible = false
                swipeToRefresh?.isRefreshing = false
                errorGroup?.isVisible = false
                emptyGroup?.isVisible = false
                content?.isVisible = true
            }

            SideEffect.HideLoadingMore -> Unit
            SideEffect.ShowLoadingMore -> Unit
        }
    }
}
