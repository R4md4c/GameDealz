package de.r4md4c.gamedealz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a store or a shop.
 */
@Entity
data class Store(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val selected: Boolean = false
)
