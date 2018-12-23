package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.CountryModel

/**
 * Gets countries under specific region.
 */
interface GetCountriesUnderRegionUseCase : UseCase<TypeParameter<String>, List<CountryModel>>
