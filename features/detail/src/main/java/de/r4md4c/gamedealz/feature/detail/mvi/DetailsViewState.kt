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

package de.r4md4c.gamedealz.feature.detail.mvi

import androidx.annotation.IdRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.mvi.MviState
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import de.r4md4c.gamedealz.feature.detail.PriceDetails
import de.r4md4c.gamedealz.feature.detail.R

internal sealed class Section {
    abstract val position: Int

    data class GameInfoSection(
        @StringRes val titleRes: Int = R.string.about_game,
        val imageUrl: String,
        val description: String,
        override val position: Int = 0
    ) : Section()

    data class ScreenshotSection(
        @StringRes val titleRes: Int = R.string.screenshots,
        val screenshots: List<ScreenshotModel>,
        val isExpanded: Boolean = false,
        @IntegerRes val visibleItemsInSection: Int = R.integer.screenshots_span_count,
        override val position: Int = 1
    ) : Section() {

        fun restOfScreenshots(resourcesProvider: ResourcesProvider): List<ScreenshotModel> {
            val spanCount = resourcesProvider.getInteger(visibleItemsInSection)
            return screenshots.takeLast(screenshots.size - spanCount)
        }
    }

    data class PriceSection(
        @StringRes val titleRes: Int = R.string.prices,
        val currentSortOrder: SortOrder = SortOrder.ByCurrentPrice,
        val priceDetails: List<PriceDetails>,
        override val position: Int = 2
    ) : Section()
}

sealed class SortOrder {
    object ByCurrentPrice : SortOrder() {
        override fun toString(): String = "ByCurrentPrice"
    }

    object ByHistoricalLow : SortOrder() {
        override fun toString(): String = "ByHistoricalLow"
    }
}

@IdRes
fun SortOrder.toMenuIdRes() =
    when (this) {
        SortOrder.ByCurrentPrice -> R.id.menu_item_current_best
        SortOrder.ByHistoricalLow -> R.id.menu_item_historical_low
    }

internal data class DetailsViewState(
    val sections: List<Section> = emptyList(),
    val isWatched: Boolean? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null
) : MviState
