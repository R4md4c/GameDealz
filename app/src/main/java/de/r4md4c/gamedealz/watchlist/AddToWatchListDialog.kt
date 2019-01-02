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

package de.r4md4c.gamedealz.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.PriceModel

class AddToWatchListDialog : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.layout_add_to_watch_list, container, false)

    companion object {
        fun newInstance(plainId: String, title: String, priceModel: PriceModel) = AddToWatchListDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_PLAIN_ID, plainId)
                putString(ARG_TITLE, title)
                putParcelable(ARG_PRICE_MODEL, priceModel)
            }
        }

        private const val ARG_TITLE = "title"
        private const val ARG_PLAIN_ID = "plain_id"
        private const val ARG_PRICE_MODEL = "price_model"
    }
}