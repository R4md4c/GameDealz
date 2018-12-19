package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.model.DealsResult

interface GetDealsUseCase : UseCase<PageParameter, DealsResult>
