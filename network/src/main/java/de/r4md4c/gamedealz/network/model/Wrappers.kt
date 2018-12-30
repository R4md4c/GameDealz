package de.r4md4c.gamedealz.network.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class DataWrapper<out T>(val data: T)

@JsonSerializable
data class ListWrapper<out T>(val list: List<T>, val count: Int?)
