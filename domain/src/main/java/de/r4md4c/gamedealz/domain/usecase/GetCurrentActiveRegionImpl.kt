package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.model.ActiveCountry

class GetCurrentActiveRegionImpl(private val getRegionsUseCase: GetRegionsUseCase) : GetCurrentActiveRegion {

    override suspend fun invoke(): ActiveCountry = ActiveCountry("", "")
}