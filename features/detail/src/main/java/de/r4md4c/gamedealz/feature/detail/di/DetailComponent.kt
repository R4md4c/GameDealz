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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.BindsInstance
import dagger.Component
import de.r4md4c.gamedealz.common.di.FeatureScope
import de.r4md4c.gamedealz.common.di.activity.ActivityModule
import de.r4md4c.gamedealz.common.di.state.StateMachineModule
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.feature.detail.DetailsFragment

@FeatureScope
@Component(
    modules = [
        StateMachineModule::class,
        ActivityModule::class
    ],
    dependencies = [
        CoreComponent::class,
        DetailsRetainedComponent::class
    ]
)
internal interface DetailComponent {

    fun inject(detailsFragment: DetailsFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance fragmentActivity: FragmentActivity,
            @BindsInstance fragment: Fragment,
            retainedComponent: DetailsRetainedComponent,
            coreComponent: CoreComponent
        ): DetailComponent
    }
}
