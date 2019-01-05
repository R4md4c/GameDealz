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

package de.r4md4c.gamedealz.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `Watchlist` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            |`plainId` TEXT NOT NULL,
            |`title` TEXT NOT NULL,
            |`dateAdded` INTEGER NOT NULL,
            |`lastCheckDate` INTEGER NOT NULL,
            |`currentPrice` REAL NOT NULL,
            |`targetPrice` REAL NOT NULL)
        """.trimMargin()
            )

            database.execSQL("CREATE UNIQUE INDEX `index_Watchlist_plainId` ON `Watchlist` (`plainId`)")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}