package com.example.snapshot.model

import android.graphics.Bitmap

/**
 * Represents a unique grouped individual identified across the video.
 */
data class PersonProfile(
    val personId: Int,
    val appearanceCount: Int,
    val appearances: List<AppearanceSegment>,
    val representativeShot: Bitmap,
    val bestDetection: FaceDetectionResult,
    val averageEmbedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonProfile
        return personId == other.personId && appearanceCount == other.appearanceCount
    }

    override fun hashCode(): Int {
        var result = personId
        result = 31 * result + appearanceCount
        return result
    }
}
