package de.r4md4c.gamedealz.network.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Stores(val data: List<Store>)

@JsonSerializable
data class Store(val id: String, val title: String, val color: String)
