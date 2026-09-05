package com.example.snapshot.processor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

data class ExtractedFrame(
    val frameIndex: Int,
    val totalFrames: Int,
    val timestampMs: Long,
    val bitmap: Bitmap
)

@Singleton
class VideoFrameExtractor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        const val FRAME_INTERVAL_MS = 200L // 5 FPS
    }

    suspend fun extractFrames(
        videoUri: Uri,
        onFrameExtracted: suspend (ExtractedFrame) -> Unit
    ): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            if (durationMs <= 0L) {
                return 0L
            }

            val totalFrames = ((durationMs / FRAME_INTERVAL_MS) + 1).toInt()
            var frameIndex = 0

            for (timeMs in 0L..durationMs step FRAME_INTERVAL_MS) {
                if (!coroutineContext.isActive) break

                // MediaMetadataRetriever expects microseconds
                val timeUs = timeMs * 1000L
                val frameBitmap = retriever.getFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST
                ) ?: continue

                onFrameExtracted(
                    ExtractedFrame(
                        frameIndex = frameIndex++,
                        totalFrames = totalFrames,
                        timestampMs = timeMs,
                        bitmap = frameBitmap
                    )
                )
            }

            durationMs
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
