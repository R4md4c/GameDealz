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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_error_retry_empty.*

typealias OnRetryClick = () -> Unit

/**
 * Handles visibility of the widgets depending on the state machine's SideEffect.
 */
class StateVisibilityHandler(
    private val fragment: Fragment,
    private val onRetryClick: OnRetryClick
) {

    private val content: View?
        get() = fragment.view?.findViewById(R.id.content)

    private val progress: View?
        get() = fragment.view?.findViewById(R.id.progress)

    private val swipeToRefresh: SwipeRefreshLayout?
        get() = fragment.view?.findViewById(R.id.swipeToRefresh) as? SwipeRefreshLayout

    fun onViewCreated() {
        fragment.retry?.setOnClickListener { onRetryClick() }
    }

    fun onSideEffect(sideEffect: SideEffect) {
        with(fragment) {
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
            }
        }
    }
}
