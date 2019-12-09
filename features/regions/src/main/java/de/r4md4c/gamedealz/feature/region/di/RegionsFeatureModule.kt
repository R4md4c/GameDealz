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

package de.r4md4c.gamedealz.feature.region.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.r4md4c.commonproviders.di.viewmodel.ViewModelKey
import de.r4md4c.gamedealz.feature.region.RegionSelectionViewModel

@Module
internal abstract class RegionsFeatureModule {

    @Binds
    @IntoMap
    @ViewModelKey(RegionSelectionViewModel::class)
    abstract fun bindsViewModel(it: RegionSelectionViewModel): ViewModel
}
