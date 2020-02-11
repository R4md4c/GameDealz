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

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.repository.PlainsLocalDataSource
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase
import javax.inject.Inject

/**
 * Retrieve an image url from steam.
 */
internal class GetImageUrlFromSteamUseCaseImpl @Inject constructor(
    private val plainsRepository: PlainsLocalDataSource,
    private val dateProvider: DateProvider
) : GetImageUrlUseCase {

    override suspend fun invoke(param: TypeParameter<String>?): String? {
        requireNotNull(param)

        return plainsRepository.findById(param.value)?.run {
            val shopIdInSteam = shopId.split('/').takeIf { it.size == 2 }?.run { "${get(0)}s/${get(1)}" }
                ?: return null

            // Providing the today's date only to get the same image as long as we are on the same day.
            "https://steamcdn-a.akamaihd.net/steam/$shopIdInSteam/header.jpg?t=${dateProvider.today().time}"
        }
    }
}
