package com.zc.bakamitai.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "page")
    val page: String = "",
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String = "",
)
