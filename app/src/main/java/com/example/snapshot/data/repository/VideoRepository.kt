package com.example.snapshot.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.snapshot.data.local.VideoDao
import com.example.snapshot.data.local.entity.PersonRecord
import com.example.snapshot.data.local.entity.VideoRecord
import com.example.snapshot.ml.FaceDetector
import com.example.snapshot.ml.FaceEmbedder
import com.example.snapshot.ml.QualityScorer
import com.example.snapshot.model.FaceDetectionResult
import com.example.snapshot.model.ProcessingState
import com.example.snapshot.model.VideoAnalysisResult
import com.example.snapshot.processor.AppearanceTracker
import com.example.snapshot.processor.CollageGenerator
import com.example.snapshot.processor.PersonGrouper
import com.example.snapshot.processor.VideoFrameExtractor
import com.example.snapshot.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val videoDao: VideoDao,
    private val frameExtractor: VideoFrameExtractor,
    private val faceDetector: FaceDetector,
    private val faceEmbedder: FaceEmbedder,
    private val appearanceTracker: AppearanceTracker,
    private val personGrouper: PersonGrouper,
    private val collageGenerator: CollageGenerator
) {

    fun getAllVideos(): Flow<List<VideoRecord>> = videoDao.getAllVideos()


     //Executes the full on-device pipeline off the main thread

    fun processVideo(videoUri: Uri): Flow<ProcessingState> = channelFlow {
        send(ProcessingState.ExtractingFrames(0f, 0, 100))

        appearanceTracker.reset()

        var totalDurationMs = 0L
        var framesAnalyzed = 0
        var totalExtractedFrames = 0

        // Step 1 & 2: Extract frames and detect faces
        totalDurationMs = frameExtractor.extractFrames(videoUri) { frame ->
            totalExtractedFrames = frame.totalFrames
            val frameProgress = (frame.frameIndex.toFloat() / frame.totalFrames.coerceAtLeast(1)) * 0.5f
            send(ProcessingState.ExtractingFrames(frameProgress, frame.frameIndex + 1, frame.totalFrames))

            // Run ML Kit Face Detection on the frame
            val detectedMlFaces = try {
                faceDetector.detectFaces(frame.bitmap)
            } catch (e: Exception) {
                emptyList()
            }

            val frameFaceResults = mutableListOf<FaceDetectionResult>()

            for (face in detectedMlFaces) {
                val box = face.boundingBox
                // Ensure bounding box is within frame bounds
                if (box.width() < 40 || box.height() < 40) continue

                val crop = ImageUtils.cropFaceForEmbedding(frame.bitmap, box)

                // Calculate quality score (frontality, sharpness, eyes, smile)
                val quality = QualityScorer.calculateQualityScore(
                    faceCrop = crop,
                    frameWidth = frame.bitmap.width,
                    frameHeight = frame.bitmap.height,
                    boundingBox = box,
                    eulerY = face.headEulerAngleY,
                    eulerZ = face.headEulerAngleZ,
                    eulerX = face.headEulerAngleX,
                    leftEyeOpenProb = face.leftEyeOpenProbability,
                    rightEyeOpenProb = face.rightEyeOpenProbability,
                    smileProb = face.smilingProbability
                )

                // Generate TFLite face embedding vector
                val embedding = faceEmbedder.extractEmbedding(crop)

                frameFaceResults.add(
                    FaceDetectionResult(
                        timestampMs = frame.timestampMs,
                        boundingBox = box,
                        headEulerAngleX = face.headEulerAngleX,
                        headEulerAngleY = face.headEulerAngleY,
                        headEulerAngleZ = face.headEulerAngleZ,
                        leftEyeOpenProbability = face.leftEyeOpenProbability,
                        rightEyeOpenProbability = face.rightEyeOpenProbability,
                        smilingProbability = face.smilingProbability,
                        qualityScore = quality,
                        embedding = embedding,
                        frameBitmap = frame.bitmap
                    )
                )
            }

            // Ingest into chronological appearance tracker
            appearanceTracker.processFrameDetections(frame.timestampMs, frameFaceResults)
            framesAnalyzed++
        }

        if (framesAnalyzed == 0) {
            send(ProcessingState.Error("Could not extract frames from selected video."))
            return@channelFlow
        }

        // Step 3: Finalize appearance segments
        send(ProcessingState.DetectingFaces(0.70f, framesAnalyzed, totalExtractedFrames))
        val appearanceSegments = appearanceTracker.finishTracking()
        android.util.Log.i("SnapshotPipeline", "Step 3: Found ${appearanceSegments.size} continuous appearance segments across $framesAnalyzed frames.")
        appearanceSegments.forEachIndexed { idx, seg ->
            android.util.Log.d("SnapshotPipeline", "  Segment #$idx: ${seg.startTimeMs}ms -> ${seg.endTimeMs}ms (duration: ${seg.durationMs}ms, frames: ${seg.detections.size})")
        }

        // Step 4: Group appearances into unique person profiles
        send(ProcessingState.GroupingPersons(0.85f, appearanceSegments.size))
        val persons = personGrouper.groupAppearances(appearanceSegments)
        android.util.Log.i("SnapshotPipeline", "Step 4: Grouped ${appearanceSegments.size} appearances into ${persons.size} unique people.")
        persons.forEach { person ->
            android.util.Log.i("SnapshotPipeline", "  Person #${person.personId}: ${person.appearanceCount} appearances")
        }

        if (persons.isEmpty()) {
            send(ProcessingState.Error("No distinct people detected in the video."))
            return@channelFlow
        }

        // Step 5: Render Instagram Story Collage
        send(ProcessingState.GeneratingCollage)
        val collageBitmap = collageGenerator.generateCollage(persons)

        // Cache collage bitmap locally
        val cacheFile = File(context.filesDir, "collage_${System.currentTimeMillis()}.png")
        FileOutputStream(cacheFile).use { out ->
            collageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val collageUriString = cacheFile.absolutePath

        // Step 6: Persist results in Room Database
        val totalAppearances = persons.sumOf { it.appearanceCount }
        val videoRecord = VideoRecord(
            videoUri = videoUri.toString(),
            personCount = persons.size,
            totalAppearances = totalAppearances,
            durationMs = totalDurationMs,
            collageUri = collageUriString
        )
        val insertedVideoId = videoDao.insertVideo(videoRecord)

        val personRecords = persons.map { person ->
            val personFile = File(context.filesDir, "person_${insertedVideoId}_${person.personId}.jpg")
            try {
                FileOutputStream(personFile).use { out ->
                    person.representativeShot.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            PersonRecord(
                videoId = insertedVideoId,
                personId = person.personId,
                appearanceCount = person.appearanceCount,
                bestQualityScore = person.bestDetection.qualityScore,
                representativeImageUri = if (personFile.exists()) personFile.absolutePath else null
            )
        }
        videoDao.insertPersons(personRecords)

        val analysisResult = VideoAnalysisResult(
            videoId = insertedVideoId,
            videoUri = videoUri.toString(),
            durationMs = totalDurationMs,
            totalAppearances = totalAppearances,
            personCount = persons.size,
            persons = persons,
            collageBitmap = collageBitmap,
            collageUri = collageUriString
        )

        send(ProcessingState.Success(analysisResult))
    }.catch { e ->
        e.printStackTrace()
        emit(ProcessingState.Error(e.localizedMessage ?: "Unknown error occurred during processing."))
    }.flowOn(Dispatchers.Default)
}
