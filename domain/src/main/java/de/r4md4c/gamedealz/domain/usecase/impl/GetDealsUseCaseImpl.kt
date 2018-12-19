package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.gamedealz.domain.PageParameter
import de.r4md4c.gamedealz.domain.model.DealsResult
import de.r4md4c.gamedealz.domain.model.toDealModel
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase
import de.r4md4c.gamedealz.domain.usecase.GetSelectedStoresUseCase
import de.r4md4c.gamedealz.network.repository.DealsRemoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withContext

internal class GetDealsUseCaseImpl(
    private val dealsRemoteRepository: DealsRemoteRepository,
    private val activeRegionUseCase: GetCurrentActiveRegionUseCase,
    private val selectedStoresUseCase: GetSelectedStoresUseCase
) : GetDealsUseCase {

    override suspend fun invoke(param: PageParameter?): DealsResult {
        requireNotNull(param)

        return withContext(IO) {
            val activeRegion = activeRegionUseCase()
            dealsRemoteRepository.deals(param.offset,
                param.pageSize,
                activeRegion.regionCode,
                activeRegion.country.code,
                selectedStoresUseCase().first().map { it.id }.toSet()
            )
                .run {
                    this.totalCount to page.map { it.toDealModel() }
                }
        }
    }
}