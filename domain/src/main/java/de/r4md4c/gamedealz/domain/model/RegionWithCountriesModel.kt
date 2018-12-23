package de.r4md4c.gamedealz.domain.model

data class RegionWithCountriesModel(
    val regionCode: String,
    val currency: CurrencyModel,
    val countries: List<CountryModel>
)