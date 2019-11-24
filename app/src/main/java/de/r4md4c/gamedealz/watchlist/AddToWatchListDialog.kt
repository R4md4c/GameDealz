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

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.domain.model.PriceModel
import de.r4md4c.gamedealz.domain.model.StoreModel
import kotlinx.android.synthetic.main.layout_add_to_watch_list.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddToWatchListDialog : BottomSheetDialogFragment() {

    private val addToWatchListViewModel by viewModel<AddToWatchListViewModel>()

    private val title: String by lazy { arguments!!.getString(ARG_TITLE) }

    private val plainId: String by lazy { arguments!!.getString(ARG_PLAIN_ID) }

    private val priceModel: PriceModel by lazy {
        arguments!!.getParcelable<PriceModel>(
            ARG_PRICE_MODEL
        )
    }

    private val viewNotifier: ViewNotifier by inject()

    private val bottomSheetCallback by lazy { AddToWatchListBottomSheetCallback() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.layout_add_to_watch_list, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), R.style.AppTheme_AddToWatchListDialog).apply {
            setOnShowListener {
                behavior.addBottomSheetCallback(bottomSheetCallback)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        styleNotifyMeHeader()
        priceEditText.addTextChangedListener(MoneyTextWatcher())
        setCurrentBest()
        prepareToolbar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        addToWatchListViewModel.dismiss.observe(this, Observer {
            viewNotifier.notify(getString(R.string.watchlist_added_successfully, title))
            dismiss()
        })
        addToWatchListViewModel.emptyPriceError.observe(this, Observer { priceEditText.error = it })
        addToWatchListViewModel.generalError.observe(
            this,
            Observer { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() })

        observeStores()
    }

    private fun styleNotifyMeHeader() {
        notifyMeHeader.text = getString(R.string.notify_when_price_reaches, title).let {
            val indexOfTitle = it.indexOf(title)
            SpannableString(it).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    indexOfTitle,
                    indexOfTitle + title.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun prepareToolbar() = with(toolbar) {
        setOnClickListener { dismiss() }
        setBackgroundColor(Color.TRANSPARENT)
        setNavigationIcon(R.drawable.ic_check)
        setNavigationOnClickListener { onSubmit() }
        DrawableCompat.setTint(navigationIcon!!, Color.TRANSPARENT)
        setTitle(R.string.add_to_watch_list)
        setTitleTextColor(Color.TRANSPARENT)
    }

    private fun onSubmit() {
        kotlin.runCatching {
            storesChipGroup.children.mapNotNull {
                (it as? Chip)?.takeIf { chip -> chip.isChecked }
                    ?.let { chip -> chip.tag as StoreModel }
            }.toList()
        }.onSuccess {
            addToWatchListViewModel.onSubmit(
                priceEditText.text.toString(),
                title,
                plainId,
                priceModel,
                it
            )
        }.onFailure {
            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun observeStores() {
        addToWatchListViewModel.loadStores().observe(this, Observer { stores ->

            storesChipGroup.removeAllViews()

            stores.map { store ->
                (LayoutInflater.from(activity).inflate(
                    R.layout.layout_add_to_watch_list_chip_item,
                    storesChipGroup,
                    false
                ) as Chip).also { chip ->
                    chip.text = store.name
                    chip.tag = store
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
                        (storesChipGroup.findViewById(Math.abs(it.id.hashCode())) as? Chip)?.isChecked =
                            true
                    }
                }
            }
        })
    }

    private fun setCurrentBest() {
        val currentBestData = addToWatchListViewModel.formatCurrentBestCurrencyModel(priceModel)
        currentBestData.observe(this, Observer {
            if (it == null) {
                currentBest.isVisible = false
            } else {
                currentBest.isVisible = true
                currentBest.text = TextUtils.concat(getString(R.string.current_best), " ", it)
            }
        })
    }

    inner class MoneyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(editable: Editable) {
            val s = editable.toString()
            if (s.isEmpty()) return
            priceEditText.removeTextChangedListener(this)

            val formatted = addToWatchListViewModel.formatPrice(s)
            priceEditText.setText(formatted)
            priceEditText.setSelection(formatted?.length ?: 0)
            priceEditText.addTextChangedListener(this)
        }
    }

    private inner class AddToWatchListBottomSheetCallback :
        BottomSheetBehavior.BottomSheetCallback() {
        private val startColor = Color.TRANSPARENT
        private val endColorOnToolbar = requireActivity().resolveThemeColor(R.attr.colorOnPrimary)
        private val endColor = requireActivity().resolveThemeColor(R.attr.colorPrimary)
        private val colorEvaluator = ArgbEvaluatorCompat()

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val value = MathUtils.clamp(slideOffset, 0f, 1f)
            val colorOnToolbar = colorEvaluator.evaluate(value, startColor, endColorOnToolbar)
            val toolbarColor = colorEvaluator.evaluate(value, startColor, endColor)

            with(toolbar) {
                setBackgroundColor(toolbarColor)
                setTitleTextColor(colorOnToolbar)
                DrawableCompat.setTint(navigationIcon!!, colorOnToolbar)
            }
        }

        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                STATE_HIDDEN -> dismiss()
                STATE_EXPANDED -> toolbar.setOnClickListener(null)
                else -> toolbar.setOnClickListener { dismiss() }
            }
        }
    }

    companion object {
        fun newInstance(plainId: String, title: String, priceModel: PriceModel) =
            AddToWatchListDialog().apply {
                arguments = bundleOf(
                    ARG_PLAIN_ID to plainId,
                    ARG_TITLE to title,
                    ARG_PRICE_MODEL to priceModel
                )
            }

        private const val ARG_TITLE = "title"
        private const val ARG_PLAIN_ID = "plain_id"
        private const val ARG_PRICE_MODEL = "price_model"
    }
}
