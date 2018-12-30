package de.r4md4c.gamedealz.network.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class SearchResult(val title: String, val plain: Plain)