package com.example.snapshot.model

import java.util.UUID

/**
 * Represents one continuous appearance segment of a person in the video.
 * Starts when clearly visible and ends when no longer visible (or separated by a whip-pan cut).
 */
data class AppearanceSegment(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMs: Long,
    val endTimeMs: Long,
    val detections: List<FaceDetectionResult>,
    val representativeEmbedding: FloatArray,
    val bestDetection: FaceDetectionResult
) {
    val durationMs: Long get() = endTimeMs - startTimeMs

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppearanceSegment
        if (id != other.id) return false
        if (startTimeMs != other.startTimeMs) return false
        if (endTimeMs != other.endTimeMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + startTimeMs.hashCode()
        result = 31 * result + endTimeMs.hashCode()
        return result
    }
}
