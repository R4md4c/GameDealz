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
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.PriceModel
import kotlinx.android.synthetic.main.layout_add_to_watch_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddToWatchListDialog : BottomSheetDialogFragment() {

    private val addToWatchListViewModel by viewModel<AddToWatchListViewModel>()

    private val title: String by lazy { arguments!!.getString(ARG_TITLE) }

    private val plainId: String by lazy { arguments!!.getString(ARG_PLAIN_ID) }

    private val priceModel: PriceModel by lazy { arguments!!.getParcelable<PriceModel>(ARG_PRICE_MODEL) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.layout_add_to_watch_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        addToWatchListViewModel.loadStores().observe(this, Observer { stores ->
            storesChipGroup.removeAllViews()

            stores.map { store ->
                (LayoutInflater.from(activity).inflate(
                    R.layout.layout_add_to_watch_list_chip_item,
                    storesChipGroup,
                    false
                ) as Chip).also { chip ->
                    chip.text = store.name
                    chip.tag = store.id
                    chip.id = Math.abs(store.id.hashCode())
                    chip.isChecked = true
                    storesChipGroup.addView(chip)
                }
            }

            storeAllSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) {
                    storesChipGroup.clearCheck()
                } else {
                    stores.forEach {
                        (storesChipGroup.findViewById(Math.abs(it.id.hashCode())) as? Chip)?.isChecked = true
                    }

                }
            }
        })
    }

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