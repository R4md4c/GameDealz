package de.r4md4c.gamedealz.network.model


data class Stores(val data: List<Store>)

data class Store(val id: String, val title: String, val color: String)
