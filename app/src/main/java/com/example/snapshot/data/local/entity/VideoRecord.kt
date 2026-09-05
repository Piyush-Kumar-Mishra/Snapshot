package com.example.snapshot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_records")
data class VideoRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val videoUri: String,
    val processedTimestamp: Long = System.currentTimeMillis(),
    val personCount: Int,
    val totalAppearances: Int,
    val durationMs: Long,
    val collageUri: String? = null
)
