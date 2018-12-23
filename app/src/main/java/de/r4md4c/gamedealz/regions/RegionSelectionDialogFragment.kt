package de.r4md4c.gamedealz.regions

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import kotlinx.android.synthetic.main.dialog_region_choice.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates

class RegionSelectionDialogFragment : DialogFragment() {

    private val viewModel by viewModel<RegionSelectionViewModel>()

    private var dialogView: View by Delegates.notNull()

    private val activeRegion by lazy {
        arguments?.getParcelable<ActiveRegion>(KEY_REGION)
            ?: throw IllegalStateException("RegionSelectionDialogFragment expects an active region.")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_region_choice, null)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupRegions()
        setupCountries()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        with(dialogView) {
            val selectedRegionCode = viewModel.regions.value?.let {
                it.regions[region_spinner.selectedItemPosition]
            }
            val selectedCountryCode = viewModel.countries.value?.let {
                it.countries[country_spinner.selectedItemPosition]
            }
            (selectedRegionCode to selectedCountryCode).takeIf {
                it.first != null && it.second != null
            }?.let { viewModel.onSubmitResult(it.first!!, it.second!!) }
        }
    }

    private fun submitResults(dialog: DialogInterface) {}
    private fun setupRegions() {
        viewModel.requestRegions(activeRegion)
        viewModel.regions.observe(this, Observer { regionSelectedModel ->
            with(dialogView) {
                region_spinner.adapter =
                        ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            android.R.id.text1,
                            regionSelectedModel.regions
                        )
                region_spinner.setSelection(regionSelectedModel.activeRegionIndex)

                region_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        viewModel.onRegionSelected(parent.adapter.getItem(position) as String)
                    }
                }
            }
        })
    }

    private fun setupCountries() {
        viewModel.requestCountriesUnderRegion(activeRegion)
        viewModel.countries.observe(this, Observer { selectedCountryModel ->
            with(dialogView) {
                country_spinner.adapter =
                        ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            android.R.id.text1,
                            selectedCountryModel.countries
                        )
                selectedCountryModel.activeCountryIndex?.let {
                    country_spinner.setSelection(it)
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun create(region: ActiveRegion) = RegionSelectionDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_REGION, region)
            }
        }

        private const val KEY_REGION = "region"
    }
}