package de.r4md4c.commonproviders.res

import android.content.Context
import androidx.core.content.ContextCompat

internal class AndroidResourcesProvider(private val context: Context) : ResourcesProvider {

    override fun getColor(colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

    override fun getString(stringRes: Int): String = context.resources.getString(stringRes)

    override fun getDimenPixelSize(dimensionRes: Int): Int = context.resources.getDimensionPixelSize(dimensionRes)

    override fun getDimension(dimensionRes: Int): Float = context.resources.getDimension(dimensionRes)

    override fun getInteger(integerRes: Int): Int = context.resources.getInteger(integerRes)
}