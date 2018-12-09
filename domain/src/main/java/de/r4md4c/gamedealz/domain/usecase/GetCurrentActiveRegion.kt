package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.model.ActiveCountry

/**
 * Tries to get the current stored active region, if nothing is stored it tries to guess the device's country, and returns
 * it.
 */
interface GetCurrentActiveRegion {

    suspend operator fun invoke(): ActiveCountry
}
