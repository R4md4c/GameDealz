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

import de.r4md4c.gamedealz.common.mvi.ReducibleMviResult

internal sealed class DetailsMviResult : ReducibleMviResult<DetailsViewState>

internal class LoadingResult(val showLoading: Boolean) : DetailsMviResult() {
    override fun reduce(oldState: DetailsViewState): DetailsViewState = oldState.run {
        copy(loading = showLoading)
    }
}

internal class ErrorResult(private val errorMessage: String) : DetailsMviResult() {
    override fun reduce(oldState: DetailsViewState): DetailsViewState = oldState.run {
        copy(loading = false, errorMessage = this@ErrorResult.errorMessage)
    }
}

internal class SectionsResult(private val newSections: List<Section>) : DetailsMviResult() {
    override fun reduce(oldState: DetailsViewState): DetailsViewState = oldState.run {
        copy(
            loading = false,
            errorMessage = null,
            sections = newSections.sortedBy { it.position }
        )
    }
}

internal data class SortPricesResult(private val newPricesSection: Section.PriceSection) :
    DetailsMviResult() {

    override fun reduce(oldState: DetailsViewState): DetailsViewState = oldState.run {
        copy(
            sections = sections.map {
                if (it is Section.PriceSection) {
                    newPricesSection
                } else {
                    it
                }
            }
        )
    }
}
