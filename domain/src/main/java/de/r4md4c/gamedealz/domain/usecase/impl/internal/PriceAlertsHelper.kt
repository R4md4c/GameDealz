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

package de.r4md4c.gamedealz.domain.usecase.impl.internal

import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.gamedealz.data.entity.PriceAlert
import de.r4md4c.gamedealz.data.repository.PriceAlertRepository
import de.r4md4c.gamedealz.domain.model.WatcheeNotificationModel
import java.util.concurrent.TimeUnit

internal class PriceAlertsHelper(
    private val priceAlertRepository: PriceAlertRepository,
    private val dateProvider: DateProvider
) {

    suspend fun storeNotificationModels(notificationModel: Collection<WatcheeNotificationModel>) {
        if (notificationModel.isEmpty()) return

        val priceAlertsToBeStored = notificationModel.mapNotNull {
            val watcheeId = it.watcheeModel.id ?: return@mapNotNull null
            PriceAlert(
                0, watcheeId, it.priceModel.url, it.priceModel.shop.name,
                TimeUnit.MILLISECONDS.toSeconds(dateProvider.timeInMillis())
            )
        }
        priceAlertRepository.save(priceAlertsToBeStored)
    }
}
