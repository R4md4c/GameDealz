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

package de.r4md4c.gamedealz.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import de.r4md4c.gamedealz.data.GameDealzDatabase
import de.r4md4c.gamedealz.data.migrations.MIGRATION_1_2
import de.r4md4c.gamedealz.data.migrations.Migration2To3
import de.r4md4c.gamedealz.data.migrations.Migration3To4
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Singleton
    @Provides
    internal fun provideGameDealzDatabase(context: Context): GameDealzDatabase =
        Room.databaseBuilder(
            context,
            GameDealzDatabase::class.java,
            GameDealzDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, Migration2To3(), Migration3To4())
            .build()
}
