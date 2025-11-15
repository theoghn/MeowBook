package com.theo.meowbook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cats")
data class CatListItem (
    @PrimaryKey
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)