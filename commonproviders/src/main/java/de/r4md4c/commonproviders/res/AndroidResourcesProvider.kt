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

package de.r4md4c.commonproviders.res

import android.content.Context
import androidx.core.content.ContextCompat
import javax.inject.Inject

internal class AndroidResourcesProvider @Inject constructor(private val context: Context) :
    ResourcesProvider {

    override fun getColor(colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

    override fun getString(stringRes: Int): String = context.resources.getString(stringRes)

    override fun getString(stringRes: Int, vararg args: String): String = context.resources.getString(stringRes, *args)

    override fun getDimenPixelSize(dimensionRes: Int): Int = context.resources.getDimensionPixelSize(dimensionRes)

    override fun getDimension(dimensionRes: Int): Float = context.resources.getDimension(dimensionRes)

    override fun getInteger(integerRes: Int): Int = context.resources.getInteger(integerRes)
}
