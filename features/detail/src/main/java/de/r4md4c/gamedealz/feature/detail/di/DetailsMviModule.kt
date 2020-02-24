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

package de.r4md4c.gamedealz.feature.detail.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.common.mvi.MviViewModel
import de.r4md4c.gamedealz.feature.detail.DetailsMviViewModel
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsStateStore
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState
import de.r4md4c.gamedealz.feature.detail.mvi.processor.LoadDetailsProcessor
import de.r4md4c.gamedealz.feature.detail.mvi.processor.PriceSortChangeProcessor

@Module
internal abstract class DetailsMviModule {

    @Binds
    @IntoSet
    abstract fun bindsLoadDetailsProcessor(it: LoadDetailsProcessor):
            IntentProcessor<DetailsMviEvent, DetailsViewState>

    @Binds
    @IntoSet
    abstract fun bindsPriceSortChangeProcessor(it: PriceSortChangeProcessor):
            IntentProcessor<DetailsMviEvent, DetailsViewState>

    @Binds
    abstract fun bindsDetailsMviStore(it: DetailsStateStore): ModelStore<DetailsViewState>

    @Binds
    abstract fun bindsDetailsMviViewModel(it: DetailsMviViewModel): MviViewModel<DetailsViewState, DetailsMviEvent>
}
