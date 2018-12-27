package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.PlainDetailsModel

interface GetPlainDetails : UseCase<TypeParameter<String>, PlainDetailsModel>
