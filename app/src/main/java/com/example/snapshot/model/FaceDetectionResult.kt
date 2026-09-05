package com.example.snapshot.model

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Represents a single detected face in a video frame with its ML Kit attributes and TFLite embedding.
 */
data class FaceDetectionResult(
    val timestampMs: Long,
    val boundingBox: Rect,
    val headEulerAngleX: Float,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val smilingProbability: Float?,
    val qualityScore: Float,
    val embedding: FloatArray,
    val frameBitmap: Bitmap
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceDetectionResult
        if (timestampMs != other.timestampMs) return false
        if (boundingBox != other.boundingBox) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestampMs.hashCode()
        result = 31 * result + boundingBox.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
