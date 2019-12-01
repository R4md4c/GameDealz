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

package de.r4md4c.gamedealz.core

import android.content.Context
import dagger.Subcomponent
import de.r4md4c.gamedealz.common.IDispatchers
import de.r4md4c.gamedealz.data.di.RepositoryModule
import de.r4md4c.gamedealz.domain.di.UseCaseModule
import de.r4md4c.gamedealz.network.di.RemoteRepositoryModule
import okhttp3.OkHttpClient

@Subcomponent(
    modules = [
        RemoteRepositoryModule::class,
        UseCaseModule::class,
        RepositoryModule::class
    ]
)
interface CoreComponent : HomeUseCaseComponent {

    val okHttpClient: OkHttpClient

    val applicationContext: Context

    val dispatchers: IDispatchers
}
