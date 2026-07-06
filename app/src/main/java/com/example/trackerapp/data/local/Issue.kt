package com.example.trackerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
