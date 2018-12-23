package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.VoidParameter
import de.r4md4c.gamedealz.domain.model.RegionWithCountriesModel


interface GetRegionsUseCase : UseCase<VoidParameter, List<RegionWithCountriesModel>>

