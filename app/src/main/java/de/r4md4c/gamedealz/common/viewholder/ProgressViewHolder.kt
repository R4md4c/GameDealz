package de.r4md4c.gamedealz.common.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    init {
        (itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.apply {
            isFullSpan = true
        }
    }
}