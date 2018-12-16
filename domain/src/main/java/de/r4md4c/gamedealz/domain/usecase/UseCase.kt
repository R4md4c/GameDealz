package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.Parameter

/**
 * Base interface for all Usecases.
 */
interface UseCase<in P : Parameter, out T> {

    suspend operator fun invoke(param: P? = null): T

}