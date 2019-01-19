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

package de.r4md4c.gamedealz.deals.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.deals.item.FilterItem
import kotlinx.android.synthetic.main.fragment_dialog_deals_filter.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DealsFilterDialogFragment : BottomSheetDialogFragment() {

    private val itemAdapter by lazy {
        FastItemAdapter<FilterItem>().also {
            it.withSelectable(true)
            it.setHasStableIds(true)
            it.withSelectWithItemUpdate(true)
            it.withMultiSelect(true)
        }
    }

    private val filtersViewModel by viewModel<DealsFilterViewModel>()

    override fun getTheme(): Int = R.style.AppTheme_BottomSheetDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_dialog_deals_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(content) {
            adapter = itemAdapter
        }

        itemAdapter.withSelectionListener { item, selected ->
            item?.let { filtersViewModel.onSelection(it, selected) }
        }

        toolbar.setNavigationOnClickListener { filtersViewModel.submit() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        itemAdapter.saveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            filtersViewModel.loadStores()
        }
        filtersViewModel.stores.observe(this, Observer { itemAdapter.set(it) })
        filtersViewModel.dismiss.observe(this, Observer { dismiss() })
        savedInstanceState?.let { itemAdapter.withSavedInstanceState(it) }
    }
}