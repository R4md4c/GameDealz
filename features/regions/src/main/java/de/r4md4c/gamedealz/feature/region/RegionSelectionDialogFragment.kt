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

package de.r4md4c.gamedealz.feature.region

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.r4md4c.gamedealz.common.aware.DrawerAware
import de.r4md4c.gamedealz.common.viewmodel.createAbstractSavedStateFactory
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.feature.region.di.DaggerRegionsComponent
import de.r4md4c.gamedealz.feature.regions.R
import de.r4md4c.gamedealz.feature.regions.databinding.DialogRegionChoiceBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegionSelectionDialogFragment : DialogFragment(R.layout.dialog_region_choice) {

    @Inject
    internal lateinit var viewModelFactory: RegionSelectionViewModel.Factory

    private val viewModel by viewModels<RegionSelectionViewModel> {
        createAbstractSavedStateFactory(this, arguments, viewModelFactory::create)
    }

    // A hack to fix a bug in spinners that makes them fire onItemSelected without any user interaction.
    private var skipFirstSelection: Int = 1

    private val activeRegion by lazy {
        RegionSelectionDialogFragmentArgs.fromBundle(requireArguments()).region
    }

    override fun onAttach(context: Context) {
        onInject(context)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogRegionChoiceBinding.inflate(layoutInflater, null, false)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    renderState(binding, state)
                }
            }
        }

        binding.regionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.onRegionSelected(position)
                }
            }

        binding.countrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.onCountrySelected(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()
    }

    private fun renderState(binding: DialogRegionChoiceBinding, state: RegionSelectionViewState) {
        binding.regionSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            android.R.id.text1,
            state.regionSelectionModel.regions
        )
        binding.regionSpinner.setSelection(state.regionSelectionModel.activeRegionIndex)

        binding.countrySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            android.R.id.text1,
            state.countrySelectionModel.countryDisplayNames
        )
        binding.countrySpinner.setSelection(state.countrySelectionModel.activeCountryIndex)
    }

    override fun onResume() {
        super.onResume()
        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            viewModel.submitResult()
            (requireActivity() as? DrawerAware)?.closeDrawer()
            dismiss()
        }
    }

    private fun onInject(context: Context) {
        DaggerRegionsComponent.factory()
            .create(context.coreComponent())
            .inject(this)
    }

    companion object {
        private const val STATE_REGION_INDEX = "region_index"
        private const val STATE_COUNTRY_INDEX = "country_index"
    }
}
