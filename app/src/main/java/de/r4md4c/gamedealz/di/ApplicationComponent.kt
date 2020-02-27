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

package de.r4md4c.gamedealz.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.r4md4c.commonproviders.di.CommonProvidersModule
import de.r4md4c.gamedealz.GameDealzApplication
import de.r4md4c.gamedealz.auth.di.AuthModule
import de.r4md4c.gamedealz.core.CoreComponent
import de.r4md4c.gamedealz.data.di.DaoModule
import de.r4md4c.gamedealz.data.di.DataSourceModule
import de.r4md4c.gamedealz.data.di.DatabaseModule
import de.r4md4c.gamedealz.domain.di.UseCaseModule
import de.r4md4c.gamedealz.network.di.NetworkModule
import de.r4md4c.gamedealz.network.di.RemoteRepositoryModule
import de.r4md4c.gamedealz.workmanager.di.WorkManagerModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CommonProvidersModule::class,
        WorkManagerModule::class,
        ApplicationModule::class,
        DataSourceModule::class,
        RemoteRepositoryModule::class,
        UseCaseModule::class,
        AuthModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        DaoModule::class
    ]
)
interface ApplicationComponent {

    fun inject(application: GameDealzApplication)

    fun coreComponent(): CoreComponent

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}
