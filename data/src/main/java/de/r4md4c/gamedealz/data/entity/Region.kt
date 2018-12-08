package de.r4md4c.gamedealz.data.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    indices = [Index(value = ["fk_currencyCode"])],
    foreignKeys = [ForeignKey(
        entity = Currency::class,
        childColumns = ["fk_currencyCode"],
        parentColumns = ["currencyCode"],
        deferred = true,
        onDelete = CASCADE
    )]
)
data class Region(@PrimaryKey val regionCode: String, @ColumnInfo(name = "fk_currencyCode") val currencyCode: String)

data class RegionWithCountries(
    @Embedded
    val region: Region,

    @Embedded
    val currency: Currency,

    @Relation(entity = Country::class, entityColumn = "regionCode", parentColumn = "regionCode")
    val countries: Set<Country>
)