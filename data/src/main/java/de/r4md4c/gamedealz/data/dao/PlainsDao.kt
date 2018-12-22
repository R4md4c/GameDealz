package de.r4md4c.gamedealz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.r4md4c.gamedealz.data.entity.Plain

@Dao
internal interface PlainsDao {

    @Query("SELECT COUNT(*) FROM Plain")
    suspend fun count(): Int

    @Query("SELECT * FROM Plain WHERE id = :id")
    suspend fun findOne(id: String): Plain?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plains: List<Plain>)
}