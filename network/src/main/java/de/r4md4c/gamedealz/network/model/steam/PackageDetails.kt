package de.r4md4c.gamedealz.network.model.steam

data class AppId(val id: String, val name: String)

data class PackageDetails(val apps: List<AppId>?)
