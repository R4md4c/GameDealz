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

package de.r4md4c.gamedealz.feature.home.item

import android.annotation.SuppressLint
import android.view.View
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import de.r4md4c.gamedealz.common.state.OnRetryClick
import de.r4md4c.gamedealz.feature.home.R
import de.r4md4c.gamedealz.feature.home.databinding.LayoutDrawerRetryBinding

class ErrorDrawerItem(
    private val retryText: String,
    private val onRetryClick: OnRetryClick
) : PrimaryDrawerItem() {

    override fun getLayoutRes(): Int = R.layout.layout_drawer_retry

    @SuppressLint("ResourceType")
    override fun getType(): Int = R.layout.layout_drawer_retry

    override fun getViewHolder(v: View?): ViewHolder {
        return super.getViewHolder(v)
    }

    override fun onPostBindView(drawerItem: IDrawerItem<*, *>?, view: View?) {
        super.onPostBindView(drawerItem, view)
    }

    override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any?>) {
        val binding = LayoutDrawerRetryBinding.bind(viewHolder.itemView)
        binding.errorText.text = retryText
        binding.retry.setOnClickListener { onRetryClick() }
    }
}
