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

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.r4md4c.gamedealz.common.mvi.Intent
import de.r4md4c.gamedealz.feature.home.mvi.intent.InitIntent
import de.r4md4c.gamedealz.feature.home.mvi.viewevent.HomeMviViewEvent
import de.r4md4c.gamedealz.feature.home.mvi.viewevent.InitViewEvent
import de.r4md4c.gamedealz.feature.home.state.HomeMviViewState

@AssistedModule
@Module(includes = [AssistedInject_HomeMviIntentsModule::class])
internal abstract class HomeMviIntentsModule {

    @IntoMap
    @Binds
    @HomeMviViewEventKey(InitViewEvent::class)
    abstract fun bindsInitIntentFactory(it: InitIntent.Factory)
            : Intent.IntentAssistedFactory<HomeMviViewState, HomeMviViewEvent>
}
