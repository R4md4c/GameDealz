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

package de.r4md4c.gamedealz.domain.mapper

import dagger.Reusable
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel
import de.r4md4c.gamedealz.domain.model.ScreenshotModel
import de.r4md4c.gamedealz.network.model.steam.AppDetailsDTO
import javax.inject.Inject

@Reusable
internal class AppDetailsDTOGameArtworkMapper @Inject constructor() :
    Mapper<AppDetailsDTO, PlainDetailsModel.GameArtworkDetails> {

    override fun map(input: AppDetailsDTO): PlainDetailsModel.GameArtworkDetails =
        PlainDetailsModel.GameArtworkDetails(
            screenshots = input.screenshots.map { ScreenshotModel(it.thumbnail, it.full) },
            headerImage = input.headerImage,
            aboutGame = input.aboutGame,
            shortDescription = input.shortDescription
        )
}
