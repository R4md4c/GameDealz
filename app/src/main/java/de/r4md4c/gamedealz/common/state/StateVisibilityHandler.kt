package de.r4md4c.gamedealz.common.state

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.r4md4c.gamedealz.R
import kotlinx.android.synthetic.main.layout_error_retry_empty.*

typealias OnRetryClick = () -> Unit

class StateVisibilityHandler(
    private val fragment: Fragment,
    private val onRetryClick: OnRetryClick
) {
    init {
        fragment.retry?.setOnClickListener { onRetryClick() }
    }

    private val content: View?
        get() = fragment.view?.findViewById(R.id.content)

    private val progress: View?
        get() = fragment.view?.findViewById(R.id.progress)

    private val swipeToRefresh: SwipeRefreshLayout?
        get() = fragment.view?.findViewById(R.id.swipeToRefresh) as? SwipeRefreshLayout

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
            }
        }
    }
}