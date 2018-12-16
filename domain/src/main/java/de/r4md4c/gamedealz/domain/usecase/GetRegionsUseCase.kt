package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.data.entity.RegionWithCountries
import de.r4md4c.gamedealz.domain.VoidParameter


interface GetRegionsUseCase : UseCase<VoidParameter, List<RegionWithCountries>>

