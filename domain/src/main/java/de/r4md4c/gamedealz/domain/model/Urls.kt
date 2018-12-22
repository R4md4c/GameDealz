package de.r4md4c.gamedealz.domain.model

/**
 * @param buyUrl the deal url in the actual target site. (e.g. steam or origin)
 * @param gameInfo The deal url at IsThereAnyDeal
 */
data class Urls(val buyUrl: String, val gameInfo: String, val imageUrl: String? = null)