package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.TypeParameter

data class ChangeActiveRegionParameter(val regionCode: String, val countryCode: String)

interface ChangeActiveRegionUseCase : UseCase<TypeParameter<ChangeActiveRegionParameter>, Unit>
