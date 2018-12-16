package de.r4md4c.gamedealz.domain

sealed class Parameter

object VoidParameter : Parameter()

data class TypeParameter<T>(val value: T) : Parameter()

data class CollectionParameter<T>(val list: Collection<T>) : Parameter()
