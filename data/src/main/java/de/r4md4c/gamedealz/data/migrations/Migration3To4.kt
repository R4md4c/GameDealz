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
import androidx.sqlite.db.transaction

private const val VERSION_3 = 3
private const val VERSION_4 = 4

internal class Migration3To4 : Migration(VERSION_3, VERSION_4) {

    override fun migrate(database: SupportSQLiteDatabase) = database.transaction {
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Price` (`fk_plainId` TEXT NOT NULL, 
            `fk_storeId` TEXT NOT NULL,
            `newPrice` REAL NOT NULL,
            `oldPrice` REAL NOT NULL,
            `priceCutPercentage` INTEGER NOT NULL,
            `dateCreated` INTEGER NOT NULL, 
            `dateUpdated` INTEGER NOT NULL,
             PRIMARY KEY(`fk_plainId`, `fk_storeId`), 
             FOREIGN KEY(`fk_storeId`) REFERENCES 
             `Store`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , 
             FOREIGN KEY(`fk_plainId`) REFERENCES `Plain`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )
        """
        )

        execSQL("CREATE INDEX IF NOT EXISTS `index_Price_fk_plainId` ON `Price` (`fk_plainId`)")

        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `HistoricalLowPrice` (`fk_plainId` TEXT NOT NULL, 
            `fk_storeId` TEXT NOT NULL,
            `price` REAL NOT NULL,
            `priceCutPercentage` INTEGER NOT NULL,
            `priceDate` INTEGER NOT NULL,
            `dateCreated` INTEGER NOT NULL,
            `dateUpdated` INTEGER NOT NULL,
            PRIMARY KEY(`fk_plainId`, `fk_storeId`),
            FOREIGN KEY(`fk_storeId`) REFERENCES `Store`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION ,
            FOREIGN KEY(`fk_plainId`) REFERENCES `Plain`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )
        """
        )

        execSQL("CREATE INDEX IF NOT EXISTS `index_HistoricalLowPrice_fk_plainId` ON `HistoricalLowPrice` (`fk_plainId`)")
    }
}
