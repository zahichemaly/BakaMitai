package com.zc.bakamitai.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Schedule(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "date")
    val date: Long = Date().time,
    @ColumnInfo(name = "isScheduled")
    val isScheduled: Boolean = false,
)
