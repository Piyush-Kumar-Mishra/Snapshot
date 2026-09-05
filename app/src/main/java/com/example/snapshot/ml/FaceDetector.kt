package com.example.snapshot.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device face detection using Google ML Kit.
 * Extracts bounding boxes, 3D head pose angles, and facial expression probabilities.
 */
@Singleton
class FaceDetector @Inject constructor() {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setMinFaceSize(0.10f) // Focus on clearly visible people, ignore background specs
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * Detects faces in a frame bitmap asynchronously.
     */
    suspend fun detectFaces(bitmap: Bitmap): List<Face> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (continuation.isActive) {
                    continuation.resume(faces)
                }
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
    }

    fun close() {
        try {
            detector.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
