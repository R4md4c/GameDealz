package de.r4md4c.gamedealz.common.navigator

interface Navigator {

    fun navigate(uri: String)

    fun navigateUp(): Boolean
}