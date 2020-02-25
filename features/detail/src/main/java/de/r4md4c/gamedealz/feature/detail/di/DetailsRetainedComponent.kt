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

import dagger.BindsInstance
import dagger.Component
import de.r4md4c.commonproviders.di.viewmodel.ScopedComponent
import de.r4md4c.gamedealz.common.aware.LifecycleAware
import de.r4md4c.gamedealz.common.di.ViewModelScope
import de.r4md4c.gamedealz.common.mvi.MviViewModel
import de.r4md4c.gamedealz.common.mvi.UIEventsDispatcher
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.feature.detail.DetailsFragmentArgs
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsMviEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsUIEvent
import de.r4md4c.gamedealz.feature.detail.mvi.DetailsViewState

@ViewModelScope
@Component(
    modules = [DetailsMviModule::class],
    dependencies = [CoreComponent::class]
)
internal abstract class DetailsRetainedComponent : ScopedComponent() {

    abstract val viewModel: MviViewModel<DetailsViewState, DetailsMviEvent>

    abstract val dispatcher: UIEventsDispatcher<DetailsUIEvent>

    abstract val lifecycleAware: LifecycleAware

    override fun onCleared() {
        super.onCleared()
        viewModel.onCleared()
        dispatcher.onClear()
    }

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance detailsFragmentArgs: DetailsFragmentArgs,
            coreComponent: CoreComponent
        ): DetailsRetainedComponent
    }
}
