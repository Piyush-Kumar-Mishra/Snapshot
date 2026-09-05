package com.example.snapshot.processor

import com.example.snapshot.model.AppearanceSegment
import com.example.snapshot.model.FaceDetectionResult
import com.example.snapshot.util.ImageUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks face appearances chronologically across video frames.
 * Groups contiguous face detections into continuous appearance segments.
 * Cuts/whip-pan passes (gaps > MAX_TRACKING_GAP_MS) finalize previous appearances.
 */
@Singleton
class AppearanceTracker @Inject constructor() {

    companion object {
        // Configurable calibration thresholds
        const val TRACKING_SIMILARITY_THRESHOLD = 0.60f
        const val MAX_TRACKING_GAP_MS = 350L       // Whip-pan or cut gap threshold (ends segment if missing >= 2 frames)
        const val MIN_APPEARANCE_DURATION_MS = 250L // Min duration to filter 1-frame flickers
    }

    private class ActiveTrack(
        val startTimeMs: Long,
        var lastSeenTimeMs: Long,
        val detections: MutableList<FaceDetectionResult> = mutableListOf(),
        var runningEmbedding: FloatArray
    )

    private val activeTracks = mutableListOf<ActiveTrack>()
    private val completedSegments = mutableListOf<AppearanceSegment>()

    /**
     * Resets tracker state before analyzing a new video.
     */
    fun reset() {
        activeTracks.clear()
        completedSegments.clear()
    }

    /**
     * Ingests detected faces from a single frame at the given timestamp.
     */
    fun processFrameDetections(timestampMs: Long, faces: List<FaceDetectionResult>) {
        // 1. FIRST: Finalize tracks that have been inactive for longer than MAX_TRACKING_GAP_MS.
        // This ensures gaps (cuts/whip-pans) properly end the previous appearance before new faces are matched!
        val iterator = activeTracks.iterator()
        while (iterator.hasNext()) {
            val track = iterator.next()
            if (timestampMs - track.lastSeenTimeMs > MAX_TRACKING_GAP_MS) {
                finalizeTrack(track)
                iterator.remove()
            }
        }

        val unmatchedFaces = faces.toMutableList()

        // 2. Build all (track, face, similarity) candidates and assign globally best-first.
        // This prevents the first track in the list from "stealing" a face that is a better
        // match for a later track — which was causing wrong person assignment in multi-face frames.
        val assignments = mutableListOf<Triple<ActiveTrack, FaceDetectionResult, Float>>()
        for (track in activeTracks) {
            for (face in unmatchedFaces) {
                val sim = ImageUtils.cosineSimilarity(face.embedding, track.runningEmbedding)
                if (sim >= TRACKING_SIMILARITY_THRESHOLD) {
                    assignments.add(Triple(track, face, sim))
                }
            }
        }
        // Sort by similarity descending so the strongest matches are committed first
        assignments.sortByDescending { it.third }

        val assignedTracks = mutableSetOf<ActiveTrack>()
        val assignedFaces = mutableSetOf<FaceDetectionResult>()
        for ((track, face, _) in assignments) {
            if (track in assignedTracks || face in assignedFaces) continue
            track.detections.add(face)
            track.lastSeenTimeMs = timestampMs
            updateRunningEmbedding(track, face.embedding)
            unmatchedFaces.remove(face)
            assignedTracks.add(track)
            assignedFaces.add(face)
        }

        // 3. Any unmatched faces start new active tracks
        for (newFace in unmatchedFaces) {
            activeTracks.add(
                ActiveTrack(
                    startTimeMs = timestampMs,
                    lastSeenTimeMs = timestampMs,
                    detections = mutableListOf(newFace),
                    runningEmbedding = newFace.embedding.copyOf()
                )
            )
        }
    }

    /**
     * Called at the end of the video to close all remaining tracks and return valid appearance segments.
     */
    fun finishTracking(): List<AppearanceSegment> {
        for (track in activeTracks) {
            finalizeTrack(track)
        }
        activeTracks.clear()

        val validSegments = completedSegments.filter { segment ->
            segment.durationMs >= MIN_APPEARANCE_DURATION_MS || segment.detections.size >= 2
        }

        return validSegments
    }

    private fun finalizeTrack(track: ActiveTrack) {
        if (track.detections.isEmpty()) return

        // Best detection in this segment has the highest composite quality score
        val bestDetection = track.detections.maxByOrNull { it.qualityScore } ?: track.detections.first()

        // Representative embedding: average of top 3 highest quality detections
        val topDetections = track.detections.sortedByDescending { it.qualityScore }.take(3)
        val repEmbedding = FloatArray(topDetections.first().embedding.size)
        for (det in topDetections) {
            for (i in repEmbedding.indices) {
                repEmbedding[i] += det.embedding[i]
            }
        }
        ImageUtils.l2Normalize(repEmbedding)

        completedSegments.add(
            AppearanceSegment(
                startTimeMs = track.startTimeMs,
                endTimeMs = track.lastSeenTimeMs,
                detections = track.detections.toList(),
                representativeEmbedding = repEmbedding,
                bestDetection = bestDetection
            )
        )
    }

    private fun updateRunningEmbedding(track: ActiveTrack, newEmbedding: FloatArray) {
        val n = track.detections.size
        for (i in track.runningEmbedding.indices) {
            track.runningEmbedding[i] = (track.runningEmbedding[i] * (n - 1) + newEmbedding[i]) / n
        }
        ImageUtils.l2Normalize(track.runningEmbedding)
    }
}
