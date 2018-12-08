package de.r4md4c.gamedealz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Region::class,
        childColumns = ["regionCode"],
        parentColumns = ["code"],
        deferred = true,
        onDelete = CASCADE
    )],
    indices = [Index(value = ["regionCode"])]
)
data class Country(
    @PrimaryKey val code: String,
    val regionCode: String
)