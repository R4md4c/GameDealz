package de.r4md4c.gamedealz.network.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class PageResult<T>(val totalCount: Int, val page: List<T>)