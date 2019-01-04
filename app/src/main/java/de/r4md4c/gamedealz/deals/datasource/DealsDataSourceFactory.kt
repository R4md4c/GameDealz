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

package de.r4md4c.gamedealz.deals.datasource

import androidx.paging.DataSource
import de.r4md4c.commonproviders.res.ResourcesProvider
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.deals.model.DealRenderModel
import de.r4md4c.gamedealz.domain.usecase.GetDealsUseCase

class DealsDataSourceFactory(
    private val getDealsUseCase: GetDealsUseCase,
    private val uiStateMachineDelegate: StateMachineDelegate,
    private val resourcesProvider: ResourcesProvider
) : DataSource.Factory<Int, DealRenderModel>() {

    override fun create(): DataSource<Int, DealRenderModel> =
        DealsDataSource(getDealsUseCase, uiStateMachineDelegate, resourcesProvider)

}