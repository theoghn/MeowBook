package com.theo.meowbook.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theo.meowbook.model.CatListItem

@Database(entities = [CatListItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
}
