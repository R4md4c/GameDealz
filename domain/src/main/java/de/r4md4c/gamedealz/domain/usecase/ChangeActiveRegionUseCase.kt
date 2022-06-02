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

package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers.IO
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.gamedealz.common.runSuspendCatching
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.data.repository.StoresLocalDataSource
import de.r4md4c.gamedealz.domain.model.ActiveRegion
import de.r4md4c.gamedealz.network.repository.StoresRemoteRepository
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class ChangeActiveRegionParameter(val regionCode: String, val countryCode: String)

class ChangeActiveRegionUseCase @Inject constructor(
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val storesRemoteRepository: StoresRemoteRepository,
    private val storesLocalRepository: StoresLocalDataSource,
    private val getCurrentActiveRegionUseCase: GetCurrentActiveRegionUseCase
) {

    suspend fun invoke(activeRegionParam: ChangeActiveRegionParameter) {
        val storedActiveRegion = withContext(IO) {
            getCurrentActiveRegionUseCase.invoke()
        }

        if (activeRegionParam.isSameAs(storedActiveRegion)) {
            Timber.d("Active regions didn't change, skipping ...")
            return
        }

        runSuspendCatching {
            val remoteStores =
                storesRemoteRepository.stores(
                    activeRegionParam.regionCode,
                    activeRegionParam.countryCode
                )
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
