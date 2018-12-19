package de.r4md4c.gamedealz.network.model

data class PageResult<T>(val totalCount: Int, val page: List<T>)