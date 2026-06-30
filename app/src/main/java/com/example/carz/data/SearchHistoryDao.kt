package com.example.carz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistory)

    @Delete
    suspend fun delete(history: SearchHistory)

    @Update
    suspend fun update(history: SearchHistory)

    @Query("UPDATE search_history SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePin(id: Int, pinned: Boolean)

    @Query("DELETE FROM search_history WHERE name = :name")
    suspend fun deleteByName(name: String)
}
