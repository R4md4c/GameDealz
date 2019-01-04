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

package de.r4md4c.gamedealz.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.image.GlideApp
import de.r4md4c.gamedealz.search.model.SearchItemRenderModel
import kotlinx.android.synthetic.main.layout_search_result_item.view.*

class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun onBind(searchResultModel: SearchItemRenderModel) {
        with(itemView) {
            name.text = searchResultModel.title
            currentBest.text = searchResultModel.currentBest
            historicalLow.text = searchResultModel.historicalLow

            GlideApp.with(image)
                .load(searchResultModel.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(image)
        }
    }


}
