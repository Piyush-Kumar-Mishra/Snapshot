package com.example.snapshot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.snapshot.data.local.entity.PersonRecord
import com.example.snapshot.data.local.entity.VideoRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideo(video: VideoRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersons(persons: List<PersonRecord>): List<Long>

    @Query("SELECT * FROM video_records ORDER BY processedTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoRecord>>

    @Query("SELECT * FROM video_records WHERE id = :videoId")
    fun getVideoById(videoId: Long): VideoRecord?

    @Query("SELECT * FROM person_records WHERE videoId = :videoId ORDER BY personId ASC")
    fun getPersonsForVideo(videoId: Long): List<PersonRecord>

    @Query("DELETE FROM video_records WHERE id = :videoId")
    fun deleteVideo(videoId: Long): Int
}
