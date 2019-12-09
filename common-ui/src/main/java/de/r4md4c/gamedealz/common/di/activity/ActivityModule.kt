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

package de.r4md4c.gamedealz.common.di.activity

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.Module
import dagger.Provides
import de.r4md4c.commonproviders.di.CommonProvidersActivityModule
import de.r4md4c.gamedealz.common.R

@Module(includes = [ActivityBindsModule::class, CommonProvidersActivityModule::class])
object ActivityModule {

    @Provides
    fun provideNavController(fragmentActivity: FragmentActivity): NavController =
        fragmentActivity.findNavController(R.id.nav_host_fragment)
}
