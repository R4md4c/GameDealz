package de.r4md4c.gamedealz.network.model

data class DataWrapper<out T>(val data: T)

data class ListWrapper<out T>(val list: List<T>, val count: Int?)
