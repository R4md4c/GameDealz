package de.r4md4c.gamedealz.domain.model

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Currency
import de.r4md4c.gamedealz.data.entity.Region

data class ActiveRegion(val region: Region, val country: Country, val currency: Currency)
