package com.zc.bakamitai.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "page")
    val page: String = "",
    @ColumnInfo(name = "date")
    val date: Long = Date().time,
    @ColumnInfo(name = "isScheduled")
    var isScheduled: Boolean = false,
)
