package de.r4md4c.gamedealz.utils.navigator

interface Navigator {

    fun navigate(uri: String)

    fun navigateUp(): Boolean
}