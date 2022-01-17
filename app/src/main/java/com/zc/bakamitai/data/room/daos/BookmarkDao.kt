package com.zc.bakamitai.data.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zc.bakamitai.data.room.entities.Bookmark

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAll(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE id = :id")
    fun getById(id: String): Bookmark?

    @Query("SELECT EXISTS(SELECT * FROM bookmark WHERE id = :id)")
    fun isExists(id: String): Boolean

    @Insert
    fun insert(bookmark: Bookmark)

    @Query("DELETE FROM bookmark WHERE id = :id")
    fun delete(id: String)
}
