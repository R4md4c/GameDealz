package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.repository.PlainsRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.usecase.GetImageUrlUseCase

/**
 * Retrieve an image url from steam.
 */
internal class GetImageUrlFromSteamUseCaseImpl(
    private val plainsRepository: PlainsRepository,
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