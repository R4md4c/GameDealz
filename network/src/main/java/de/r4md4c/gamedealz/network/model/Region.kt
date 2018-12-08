package de.r4md4c.gamedealz.network.model

data class Region(val countries: List<String>, val currency: Currency)

data class Currency(val code: String, val html: String, val sign: String)
