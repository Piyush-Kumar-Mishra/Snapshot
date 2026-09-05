package com.example.snapshot.model

import android.graphics.Bitmap

data class VideoAnalysisResult(
    val videoId: Long = 0L,
    val videoUri: String,
    val durationMs: Long,
    val totalAppearances: Int,
    val personCount: Int = persons.size,
    val persons: List<PersonProfile> = emptyList(),
    val collageBitmap: Bitmap,
    val collageUri: String? = null
)

sealed interface ProcessingState {
    object Idle : ProcessingState
    data class ExtractingFrames(val progress: Float, val currentFrame: Int, val totalFrames: Int) : ProcessingState
    data class DetectingFaces(val progress: Float, val framesProcessed: Int, val totalFrames: Int) : ProcessingState
    data class GroupingPersons(val progress: Float, val appearancesFound: Int) : ProcessingState
    object GeneratingCollage : ProcessingState
    data class Success(val result: VideoAnalysisResult) : ProcessingState
    data class Error(val message: String) : ProcessingState
}
