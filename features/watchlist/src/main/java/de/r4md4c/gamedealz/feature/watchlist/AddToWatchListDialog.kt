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

package de.r4md4c.gamedealz.feature.watchlist

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
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
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.r4md4c.commonproviders.di.viewmodel.ViewModelFactoryCreator
import de.r4md4c.commonproviders.extensions.resolveThemeColor
import de.r4md4c.gamedealz.common.exhaustive
import de.r4md4c.gamedealz.common.notifications.ViewNotifier
import de.r4md4c.gamedealz.core.coreComponent
import de.r4md4c.gamedealz.domain.model.ShopModel
import de.r4md4c.gamedealz.feature.watchlist.AddToWatchListDialogArgs.Companion.fromBundle
import de.r4md4c.gamedealz.feature.watchlist.databinding.LayoutAddToWatchListBinding
import de.r4md4c.gamedealz.feature.watchlist.di.DaggerWatchlistComponent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

internal class AddToWatchListDialog : BottomSheetDialogFragment() {

    private val title: String by lazy { fromBundle(requireArguments()).title }

    private val toolbar: Toolbar by lazy {
        requireView().findViewById(R.id.toolbar) as Toolbar
    }

    private var isChangingCheckSwitch = false

    private val addToWatchListViewModel by viewModels<AddToWatchListViewModel> {
        viewModelFactory.create(this, requireArguments())
    }

    private var binding: LayoutAddToWatchListBinding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactoryCreator

    @Inject
    lateinit var viewNotifier: ViewNotifier

    @Inject
    lateinit var requestNotificationPermissions: RequestNotificationPermissions

    private val bottomSheetCallback by lazy { AddToWatchListBottomSheetCallback() }

    override fun onAttach(context: Context) {
        DaggerWatchlistComponent.factory()
            .create(requireActivity(), this, context.coreComponent())
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_add_to_watch_list, container, false).also {
        binding = LayoutAddToWatchListBinding.bind(it)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                binding = null
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext()).apply {
            setOnShowListener {
                behavior.addBottomSheetCallback(bottomSheetCallback)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        styleNotifyMeHeader()
        binding!!.priceEditText.addTextChangedListener(MoneyTextWatcher())
        prepareToolbar()
        observeUIModel()
        observeUIEvent()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding!!.storeAllSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChangingCheckSwitch) {
                addToWatchListViewModel.onAllStoresChecked(isChecked)
            }
        }
    }

    private fun styleNotifyMeHeader() {
        binding!!.notifyMeHeader.text = getString(R.string.notify_when_price_reaches, title).let {
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
        viewLifecycleOwner.lifecycleScope.launch {
            val isAccepted = requestNotificationPermissions.requestNotificationPermission()
            Timber.i("Notifications permissions result: $isAccepted")
            addToWatchListViewModel.onSubmit(binding!!.priceEditText.text.toString())
        }
    }

    private fun observeUIModel() {
        addToWatchListViewModel.addToWatchlistUIModel.observe(
            viewLifecycleOwner,
            Observer { uiModel ->
                setCurrentBest(uiModel.currentBest)
                renderStores(uiModel.toggledStoreMap)
                isChangingCheckSwitch = true
                binding!!.storeAllSwitch.isChecked = uiModel.areAllStoresMarked
                isChangingCheckSwitch = false
            })
    }

    private fun observeUIEvent() {
        addToWatchListViewModel.uiEvents.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                AddToWatchListViewModel.UIEvent.ShowLoading -> binding!!.progress.isVisible = true
                AddToWatchListViewModel.UIEvent.HideLoading -> binding!!.progress.isVisible = false
                AddToWatchListViewModel.UIEvent.Dismiss -> {
                    viewNotifier.notify(getString(R.string.watchlist_added_successfully, title))
                    dismiss()
                }

                is AddToWatchListViewModel.UIEvent.ShowError -> {
                    viewNotifier.notify(event.errorString)
                }

                is AddToWatchListViewModel.UIEvent.PriceError -> {
                    binding!!.priceEditText.error = event.errorString
                }
            }.exhaustive
        })
    }

    private fun renderStores(stores: Map<ShopModel, Boolean>) {
        binding!!.storesChipGroup.removeAllViews()

        stores.forEach { (store, isChecked) ->
            (LayoutInflater.from(requireActivity()).inflate(
                R.layout.layout_add_to_watch_list_chip_item,
                binding!!.storesChipGroup,
                false
            ) as Chip).also { chip ->
                chip.text = store.name
                chip.tag = store
                chip.id = abs(store.id.hashCode())
                chip.isChecked = isChecked
                binding!!.storesChipGroup.addView(chip)
                chip.setOnCheckedChangeListener { buttonView, isChecked ->
                    addToWatchListViewModel.onStoreChipToggled(
                        buttonView.tag as ShopModel,
                        isChecked
                    )
                }
            }
        }
    }

    private fun setCurrentBest(currentBestPrice: String?) {
        if (currentBestPrice == null) {
            binding!!.currentBest.isVisible = false
        } else {
            binding!!.currentBest.isVisible = true
            binding!!.currentBest.text =
                TextUtils.concat(getString(R.string.current_best), " ", currentBestPrice)
        }
    }

    inner class MoneyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(editable: Editable) {
            val s = editable.toString()
            if (s.isEmpty()) return
            binding!!.priceEditText.removeTextChangedListener(this)

            val formatted = addToWatchListViewModel.formatPrice(s)
            binding!!.priceEditText.setText(formatted)
            binding!!.priceEditText.setSelection(formatted?.length ?: 0)
            binding!!.priceEditText.addTextChangedListener(this)
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
}
