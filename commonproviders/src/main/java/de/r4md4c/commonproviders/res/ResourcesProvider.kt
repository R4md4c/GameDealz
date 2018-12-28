package de.r4md4c.commonproviders.res

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes

/**
 * A wrapper around the needed functionality of [android.content.res.Resources]
 */
interface ResourcesProvider {

    fun getColor(@ColorRes colorRes: Int): Int

    fun getString(@StringRes stringRes: Int): String

    fun getDimenPixelSize(@DimenRes dimensionRes: Int): Int
}
