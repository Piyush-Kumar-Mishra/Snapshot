package com.example.snapshot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_records",
    foreignKeys = [
        ForeignKey(
            entity = VideoRecord::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("videoId")]
)
data class PersonRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val videoId: Long,
    val personId: Int,
    val appearanceCount: Int,
    val bestQualityScore: Float,
    val representativeImageUri: String? = null
)
