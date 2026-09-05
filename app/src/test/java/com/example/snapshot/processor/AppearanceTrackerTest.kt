package com.example.snapshot.processor

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.snapshot.model.FaceDetectionResult
import com.example.snapshot.util.ImageUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class AppearanceTrackerTest {

    private lateinit var tracker: AppearanceTracker
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setUp() {
        tracker = AppearanceTracker()
        mockBitmap = Mockito.mock(Bitmap::class.java)
    }

    private fun createFace(timeMs: Long, vectorSeed: Float, quality: Float = 0.8f): FaceDetectionResult {
        val embedding = FloatArray(128) { i -> if (i == 0) vectorSeed else 0.05f }
        ImageUtils.l2Normalize(embedding)

        return FaceDetectionResult(
            timestampMs = timeMs,
            boundingBox = Mockito.mock(Rect::class.java),
            headEulerAngleX = 0f,
            headEulerAngleY = 0f,
            headEulerAngleZ = 0f,
            leftEyeOpenProbability = 0.9f,
            rightEyeOpenProbability = 0.9f,
            smilingProbability = 0.7f,
            qualityScore = quality,
            embedding = embedding,
            frameBitmap = mockBitmap
        )
    }

    @Test
    fun testContinuousFramesCountAsSingleAppearance() {
        tracker.reset()

        // 4 consecutive frames at 200ms interval (0s to 0.6s)
        tracker.processFrameDetections(0L, listOf(createFace(0L, 1.0f)))
        tracker.processFrameDetections(200L, listOf(createFace(200L, 1.0f)))
        tracker.processFrameDetections(400L, listOf(createFace(400L, 1.0f)))
        tracker.processFrameDetections(600L, listOf(createFace(600L, 1.0f)))

        val appearances = tracker.finishTracking()

        assertEquals("Should produce exactly 1 continuous appearance", 1, appearances.size)
        assertEquals(0L, appearances[0].startTimeMs)
        assertEquals(600L, appearances[0].endTimeMs)
    }

    @Test
    fun testWhipPanCutCreatesMultipleAppearances() {
        tracker.reset()

        // Appearance 1: 0ms to 400ms
        tracker.processFrameDetections(0L, listOf(createFace(0L, 1.0f)))
        tracker.processFrameDetections(200L, listOf(createFace(200L, 1.0f)))
        tracker.processFrameDetections(400L, listOf(createFace(400L, 1.0f)))

        // Whip-pan cut: gap of 1000ms with no faces (> MAX_TRACKING_GAP_MS of 600ms)
        // Appearance 2: 1400ms to 1800ms
        tracker.processFrameDetections(1400L, listOf(createFace(1400L, 1.0f)))
        tracker.processFrameDetections(1600L, listOf(createFace(1600L, 1.0f)))
        tracker.processFrameDetections(1800L, listOf(createFace(1800L, 1.0f)))

        val appearances = tracker.finishTracking()

        assertEquals("Whip pan should split into 2 distinct appearances", 2, appearances.size)
        assertEquals(0L, appearances[0].startTimeMs)
        assertEquals(400L, appearances[0].endTimeMs)
        assertEquals(1400L, appearances[1].startTimeMs)
        assertEquals(1800L, appearances[1].endTimeMs)
    }

    @Test
    fun testSimultaneousFacesTrackSeparately() {
        tracker.reset()

        // Two people (Person A with seed 1.0, Person B with seed -1.0) sharing the frame
        for (timeMs in listOf(1000L, 1200L, 1400L)) {
            val faceA = createFace(timeMs, 1.0f)
            val faceB = createFace(timeMs, -1.0f)
            tracker.processFrameDetections(timeMs, listOf(faceA, faceB))
        }

        val appearances = tracker.finishTracking()

        assertEquals("Two concurrent people in same frame must yield 2 separate appearances", 2, appearances.size)
    }

    @Test
    fun testSingleSampledMultiPersonFrameCountsEveryPerson() {
        tracker.reset()

        // A brief shot can be sampled once at 5 FPS. Each person in that same
        // frame is a legitimate appearance, unlike an isolated one-frame flicker.
        tracker.processFrameDetections(
            1000L,
            listOf(createFace(1000L, 1.0f), createFace(1000L, -1.0f))
        )

        assertEquals(2, tracker.finishTracking().size)
    }

    @Test
    fun testSingleSampledIsolatedFaceIsStillFilteredAsNoise() {
        tracker.reset()
        tracker.processFrameDetections(1000L, listOf(createFace(1000L, 1.0f)))

        assertEquals(0, tracker.finishTracking().size)
    }
}
