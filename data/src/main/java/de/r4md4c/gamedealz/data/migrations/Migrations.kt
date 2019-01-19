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
    override fun migrate(database: SupportSQLiteDatabase) = with(database) {
        beginTransaction()
        try {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `Watchlist` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`plainId` TEXT NOT NULL, `title` TEXT NOT NULL,
                |`dateAdded` INTEGER NOT NULL,
                |`lastCheckDate` INTEGER NOT NULL,
                |`lastFetchedPrice` REAL NOT NULL,
                |`lastFetchedStoreName` TEXT NOT NULL,
                |`targetPrice` REAL NOT NULL,
                |`regionCode` TEXT NOT NULL,
                |`countryCode` TEXT NOT NULL,
                |`currencyCode` TEXT NOT NULL)
                |""".trimMargin()
            )

            execSQL("CREATE UNIQUE INDEX `index_Watchlist_plainId` ON `Watchlist` (`plainId`)")

            execSQL(
                """CREATE TABLE IF NOT EXISTS `watchlist_store_join`
                |(`watcheeId` INTEGER NOT NULL,
                |`storeId` TEXT NOT NULL, PRIMARY KEY(`watcheeId`, `storeId`),
                |FOREIGN KEY(`watcheeId`) REFERENCES `Watchlist`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""".trimMargin()
            )

            execSQL("CREATE  INDEX `index_watchlist_store_join_storeId` ON `watchlist_store_join` (`storeId`)")


            execSQL(
                """CREATE TABLE IF NOT EXISTS `PriceAlert` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`watcheeId` INTEGER NOT NULL,
                |`buyUrl` TEXT NOT NULL,
                |`storeName` TEXT NOT NULL,
                |`dateCreated` INTEGER NOT NULL,
                |FOREIGN KEY(`watcheeId`) REFERENCES `Watchlist`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""".trimMargin()
            )
            execSQL("CREATE UNIQUE INDEX `index_PriceAlert_watcheeId` ON `PriceAlert` (`watcheeId`)")

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}