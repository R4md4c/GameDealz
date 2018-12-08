package de.r4md4c.gamedealz.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class Region(@PrimaryKey val code: String)

data class RegionWithCountries(
    @Embedded
    val region: Region,
    @Relation(entity = Country::class, entityColumn = "regionCode", parentColumn = "code")
    val countries: Set<Country>
)