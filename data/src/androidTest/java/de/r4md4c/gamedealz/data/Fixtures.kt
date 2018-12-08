package de.r4md4c.gamedealz.data

import de.r4md4c.gamedealz.data.entity.Country
import de.r4md4c.gamedealz.data.entity.Region

object Fixtures {

    fun region(code: String) = Region(code)

    fun country(code: String, region: Region) = Country(code, region.code)

}
