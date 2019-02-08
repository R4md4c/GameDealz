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

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.deals.item.FilterItem
import kotlinx.android.synthetic.main.fragment_dialog_deals_filter.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DealsFilterDialogFragment : BottomSheetDialogFragment() {

    private val filtersViewModel by viewModel<DealsFilterViewModel>()

    override fun getTheme(): Int = R.style.AppTheme_FilterBottomSheetDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_dialog_deals_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        none.setOnClickListener { onNoneClick() }
        all.setOnClickListener { onAllClick() }
        search.addTextChangedListener { editable ->
            filtersViewModel.onSearchTextChanged(editable.toString())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            filtersViewModel.loadStores()
        }
        filtersViewModel.stores.observe(this, Observer { items ->
            addFilterItems(items)
        })
        filtersViewModel.dismiss.observe(this, Observer { dismiss() })
    }

    private fun addFilterItems(filterItems: List<FilterItem>) {
        content.removeAllViews()

        filterItems.map { filterItem ->
            LayoutInflater.from(context).inflate(R.layout.layout_deals_filter_item, content, false).apply {
                tag = filterItem
            }
        }
            .filterIsInstance(Chip::class.java)
            .forEach {
                val filterItem = it.tag as FilterItem
                it.id = filterItem.storeModel.id.hashCode()
                it.isChecked = filterItem.isSelected
                it.text = filterItem.storeModel.name
                it.chipBackgroundColor = generateChipBackgroundColor(filterItem.storeModel.color)
                content.addView(it)
                it.setOnCheckedChangeListener { buttonView, isChecked ->
                    filtersViewModel.onSelection(buttonView.tag as FilterItem, isChecked)
                }
            }
    }

    private fun generateChipBackgroundColor(@ColorInt storeModelColor: Int): ColorStateList {
        // 12% Opacity
        val colorOnSurface =
            ColorUtils.setAlphaComponent(requireContext().resolveThemeColor(R.attr.colorOnSurface), 0x1F)
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(storeModelColor, colorOnSurface)
        )
    }

    private fun onNoneClick() {
        content.clearCheck()
    }

    private fun onAllClick() {
        content.children.filterIsInstance(Chip::class.java).forEach {
            content.check(it.id)
        }
    }
}
