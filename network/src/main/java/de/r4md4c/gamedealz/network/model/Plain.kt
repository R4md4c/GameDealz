package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json

typealias IdToPlainMap = Map<String, String>

data class Plain(@Json(name = "plain") val value: String)

