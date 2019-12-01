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

package de.r4md4c.gamedealz.feature.deals.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.r4md4c.commonproviders.di.viewmodel.ViewModelKey
import de.r4md4c.gamedealz.common.di.FeatureScope
import de.r4md4c.gamedealz.common.state.StateMachineDelegate
import de.r4md4c.gamedealz.common.state.UIStateMachineDelegate
import de.r4md4c.gamedealz.feature.deals.DealsViewModel
import de.r4md4c.gamedealz.feature.deals.filter.DealsFilterViewModel

@Module
abstract class DealsFeatureModule {

    @Binds
    @IntoMap
    @ViewModelKey(DealsFilterViewModel::class)
    abstract fun bindsDealsFilterViewModel(it: DealsFilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DealsViewModel::class)
    abstract fun bindsDealsViewModel(it: DealsViewModel): ViewModel

    @FeatureScope
    @Binds
    abstract fun bindsStateMachineDelegate(it: UIStateMachineDelegate): StateMachineDelegate
}
