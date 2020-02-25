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

package de.r4md4c.gamedealz.feature.detail.mvi.processor

import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.di.ForApplication
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviResult
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.ExpandClickResult
import de.r4md4c.gamedealz.feature.detail.mvi.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

internal class ExpandClickProcessor @Inject constructor(
    private val modelState: ModelStore<DetailsViewState>,
    @ForApplication private val resourcesProvider: ResourcesProvider
) : IntentProcessor<DetailsMviEvent, DetailsViewState> {

    override fun process(viewEvent: Flow<DetailsMviEvent>): Flow<MviResult<DetailsViewState>> =
        viewEvent.filterIsInstance<DetailsMviEvent.ExpandIconClicked>()
            .mapLatest {
                val currentState = modelState.currentState
                val screenshotsSection =
                    currentState.sections.filterIsInstance<Section.ScreenshotSection>().first()
                val isExpanded = screenshotsSection.isExpanded
                val spanCount =
                    resourcesProvider.getInteger(screenshotsSection.visibleItemsInSection)

                val newScreenshotSection = if (isExpanded) {
                    screenshotsSection.copy(
                        isExpanded = false,
                        visibleScreenshots = screenshotsSection.allScreenshots.take(spanCount)
                    )
                } else {
                    screenshotsSection.copy(
                        isExpanded = true,
                        visibleScreenshots = screenshotsSection.allScreenshots
                    )
                }
                ExpandClickResult(newScreenshotSection)
            }
}
