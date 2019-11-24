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

private const val VERSION_2 = 2
private const val VERSION_3 = 3

/**
 * This Migration adds an additional primary key to the Country table. I am also clearing the Region table to force
 * a refetch of regions with their countries.
 */
internal class Migration2To3 : Migration(VERSION_2, VERSION_3) {

    override fun migrate(database: SupportSQLiteDatabase) = with(database) {
        try {
            beginTransaction()
            execSQL("DELETE FROM Region")
            execSQL("DROP TABLE Country")

            execSQL(
                """CREATE TABLE IF NOT EXISTS `Country`
                |(`code` TEXT NOT NULL, `regionCode` TEXT NOT NULL, PRIMARY KEY(`code`, `regionCode`),
                |FOREIGN KEY(`regionCode`) REFERENCES `Region`(`regionCode`)
                |ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)""".trimMargin()
            )

            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }
}
