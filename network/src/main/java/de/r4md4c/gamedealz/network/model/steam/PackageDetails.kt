package de.r4md4c.gamedealz.network.model.steam

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AppId(val id: String, val name: String)

@JsonSerializable
data class PackageDetails(val apps: List<AppId>?)
