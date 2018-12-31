/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.r4md4c.gamedealz.domain.usecase.impl

import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresRepository
import de.r4md4c.gamedealz.domain.TypeParameter
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionParameter
import de.r4md4c.gamedealz.domain.usecase.ChangeActiveRegionUseCase
import de.r4md4c.gamedealz.domain.usecase.GetCurrentActiveRegionUseCase
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class ChangeActiveRegionUseCaseImpl(
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val storesRemoteRepository: StoresRemoteRepository,
    private val storesLocalRepository: StoresRepository,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase
) : ChangeActiveRegionUseCase {

    override suspend fun invoke(param: TypeParameter<ChangeActiveRegionParameter>?) {
        val activeRegionParam = requireNotNull(param).value

        val storedActiveRegion = withContext(IO) {
            getCurrentActiveRegionUseCase()
        }

        if (activeRegionParam.isSameAs(storedActiveRegion)) {
            Timber.d("Active regions didn't change, skipping ...")
            return
        }

        runCatching {
            val remoteStores =
                storesRemoteRepository.stores(activeRegionParam.regionCode, activeRegionParam.countryCode)
            storesLocalRepository.replace(remoteStores.map { Store(it.id, it.title, it.color) })
        }.onSuccess {
            sharedPreferencesProvider.activeRegionAndCountry = activeRegionParam.regionCode to
                    activeRegionParam.countryCode
        }.onFailure {
            Timber.e(it, "Failed to change region and country.")
        }
    }

    private fun ChangeActiveRegionParameter.isSameAs(activeRegion: ActiveRegion): Boolean =
        regionCode == activeRegion.regionCode && countryCode == activeRegion.country.code

}