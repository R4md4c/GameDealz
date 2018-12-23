package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.data.repository.CountriesRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.CountryModel
import de.r4md4c.gamedealz.domain.model.toCountryModel
import de.r4md4c.gamedealz.domain.usecase.GetCountriesUnderRegionUseCase

internal class GetCountriesUnderRegionUseCaseImpl(private val countriesRepository: CountriesRepository) :
    GetCountriesUnderRegionUseCase {

    override suspend fun invoke(param: TypeParameter<String>?): List<CountryModel> {
        val regionCode = requireNotNull(param).value

        return countriesRepository.allCountriesUnderRegion(regionCode).map { it.toCountryModel() }
    }
}