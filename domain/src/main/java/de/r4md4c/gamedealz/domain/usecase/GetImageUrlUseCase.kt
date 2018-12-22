package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.TypeParameter

/**
 * Retrieves the image url for an IsThereAnyDeal plain id.
 */
internal interface GetImageUrlUseCase : UseCase<TypeParameter<String>, String?>