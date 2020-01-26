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

package de.r4md4c.gamedealz.feature.home.di.mvi

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.r4md4c.gamedealz.common.di.FeatureScope
import de.r4md4c.gamedealz.common.mvi.IntentProcessor
import de.r4md4c.gamedealz.common.mvi.ModelStore
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviModelStore
import de.r4md4c.gamedealz.feature.home.mvi.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.processor.LogoutIntentProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.NavigationEventsProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.NightModeInitIntentProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.NightModeToggleIntentProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.PriceAlertCountProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.RegionsInitIntentProcessor
import de.r4md4c.gamedealz.feature.home.mvi.processor.UserInitIntentProcessor
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState

@Module
internal abstract class HomeMviModule {

    @Binds
    @IntoSet
    abstract fun bindsLogoutIntentProcessor(it: LogoutIntentProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsNightModeInitIntentProcessor(it: NightModeInitIntentProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsPriceAlertCountProcessor(it: PriceAlertCountProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsNightModeToggleIntentProcessor(it: NightModeToggleIntentProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsRegionsInitIntentProcessor(it: RegionsInitIntentProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsUserInitIntentProcessor(it: UserInitIntentProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @Binds
    @IntoSet
    abstract fun bindsNavigationEventsProcessor(it: NavigationEventsProcessor):
            IntentProcessor<HomeMviViewEvent, HomeMviViewState>

    @FeatureScope
    @Binds
    abstract fun bindsHomeMviStore(it: HomeMviModelStore): ModelStore<HomeMviViewState>
}
