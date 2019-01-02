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

package de.r4md4c.commonproviders

import de.r4md4c.commonproviders.configuration.AndroidConfigurationImpl
import de.r4md4c.commonproviders.configuration.ConfigurationProvider
import de.r4md4c.commonproviders.coroutines.GameDealzDispatchers
import de.r4md4c.commonproviders.coroutines.IDispatchers
import de.r4md4c.commonproviders.date.AndroidDateFormatter
import de.r4md4c.commonproviders.date.DateFormatter
import de.r4md4c.commonproviders.date.DateProvider
import de.r4md4c.commonproviders.date.JavaDateProvider
import de.r4md4c.commonproviders.preferences.AndroidSharedPreferencesProvider
import de.r4md4c.commonproviders.preferences.SharedPreferencesProvider
import de.r4md4c.commonproviders.res.AndroidResourcesProvider
import de.r4md4c.commonproviders.res.ResourcesProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val COMMON_PROVIDERS = module {

    single<IDispatchers> { GameDealzDispatchers }

    factory<ConfigurationProvider> { AndroidConfigurationImpl(androidContext()) }

    factory<DateProvider> { JavaDateProvider() }

    single<SharedPreferencesProvider> { AndroidSharedPreferencesProvider(androidContext()) }

    factory<ResourcesProvider> { AndroidResourcesProvider(androidContext()) }

    factory<DateFormatter> { AndroidDateFormatter(androidContext()) }
}