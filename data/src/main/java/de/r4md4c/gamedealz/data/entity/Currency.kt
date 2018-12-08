package de.r4md4c.gamedealz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(@PrimaryKey val currencyCode: String, val sign: String)