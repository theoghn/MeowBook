package com.theo.meowbook.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theo.meowbook.model.CatListItem

@Dao
interface CatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCats(cats: List<CatListItem>)


    @Query("SELECT * FROM cats")
    suspend fun getAllCats(): List<CatListItem>


    @Query("DELETE FROM cats")
    suspend fun clearAllCats()
}
