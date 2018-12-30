package de.r4md4c.gamedealz.network.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

typealias IdToPlainMap = Map<String, String>

@JsonSerializable
data class Plain(@Json(name = "plain") val value: String)

