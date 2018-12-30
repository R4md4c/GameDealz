package de.r4md4c.gamedealz.network.model.steam

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
class ResponseWrapper<T>(val success: Boolean, val data: T?)