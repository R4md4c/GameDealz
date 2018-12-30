package de.r4md4c.gamedealz.network.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Region(val countries: List<String>, val currency: Currency)

@JsonSerializable
data class Currency(val code: String, val html: String, val sign: String)
