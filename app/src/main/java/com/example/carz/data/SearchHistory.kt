package com.example.carz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
