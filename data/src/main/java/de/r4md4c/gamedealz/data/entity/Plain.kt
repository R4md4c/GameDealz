package de.r4md4c.gamedealz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An entity that holds a mapping between IsThereAnyDeal id, and the shop's id
 */
@Entity
data class Plain(@PrimaryKey val id: String, val shopId: String)
