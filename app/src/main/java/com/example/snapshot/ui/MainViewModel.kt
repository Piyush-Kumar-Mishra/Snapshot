package com.example.snapshot.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshot.data.local.VideoDao
import com.example.snapshot.data.local.entity.VideoRecord
import com.example.snapshot.data.repository.VideoRepository
import com.example.snapshot.model.FaceDetectionResult
import com.example.snapshot.model.PersonProfile
import com.example.snapshot.model.ProcessingState
import com.example.snapshot.model.VideoAnalysisResult
import com.example.snapshot.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class AppScreen {
    SPLASH, HOME, PROCESSING, RESULT
}

enum class BottomTab {
    DASHBOARD, HISTORY
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val videoDao: VideoDao
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.DASHBOARD)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private val _analysisResult = MutableStateFlow<VideoAnalysisResult?>(null)
    val analysisResult: StateFlow<VideoAnalysisResult?> = _analysisResult.asStateFlow()

    val savedVideos: StateFlow<List<VideoRecord>> = videoRepository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var processingJob: kotlinx.coroutines.Job? = null

    fun onSplashDone() {
        _currentScreen.value = AppScreen.HOME
    }

    fun onVideoSelected(uri: Uri) {
        // Keep user on the current screen (Dashboard / History) so app remains interactive!
        _processingState.value = ProcessingState.ExtractingFrames(0.04f, 0, 100)
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            videoRepository.processVideo(uri).collect { state ->
                _processingState.value = state
                if (state is ProcessingState.Success) {
                    _analysisResult.value = state.result
                }
            }
        }
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        processingJob = null
        _processingState.value = ProcessingState.Idle
    }

    fun dismissProcessing() {
        _processingState.value = ProcessingState.Idle
    }

    fun viewResult(result: VideoAnalysisResult) {
        _analysisResult.value = result
        _currentScreen.value = AppScreen.RESULT
    }

    /**
     * Loads a previously saved analysis from Room + cached collage file.
     */
    fun loadPreviousAnalysis(record: VideoRecord) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                record.collageUri?.let { path ->
                    val file = File(path)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
            }

            if (bitmap != null) {
                val personRecords = withContext(Dispatchers.IO) {
                    videoDao.getPersonsForVideo(record.id)
                }

                val loadedProfiles = withContext(Dispatchers.IO) {
                    personRecords.map { pRec ->
                        val repBitmap = pRec.representativeImageUri?.let { path ->
                            val f = File(path)
                            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                        } ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

                        PersonProfile(
                            personId = pRec.personId,
                            appearanceCount = pRec.appearanceCount,
                            appearances = emptyList(),
                            representativeShot = repBitmap,
                            bestDetection = FaceDetectionResult(
                                timestampMs = 0L,
                                boundingBox = Rect(0, 0, repBitmap.width, repBitmap.height),
                                headEulerAngleX = 0f,
                                headEulerAngleY = 0f,
                                headEulerAngleZ = 0f,
                                leftEyeOpenProbability = null,
                                rightEyeOpenProbability = null,
                                smilingProbability = null,
                                qualityScore = pRec.bestQualityScore,
                                embedding = FloatArray(0),
                                frameBitmap = repBitmap
                            ),
                            averageEmbedding = FloatArray(0)
                        )
                    }
                }

                _analysisResult.value = VideoAnalysisResult(
                    videoId = record.id,
                    videoUri = record.videoUri,
                    durationMs = record.durationMs,
                    totalAppearances = record.totalAppearances,
                    personCount = record.personCount,
                    persons = loadedProfiles,
                    collageBitmap = bitmap,
                    collageUri = record.collageUri
                )
                _currentScreen.value = AppScreen.RESULT
            }
        }
    }

    fun saveCollageToGallery(context: Context) {
        val result = _analysisResult.value ?: return
        viewModelScope.launch {
            val uri = FileUtils.saveBitmapToGallery(context, result.collageBitmap)
            if (uri != null) {
                Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareCollage(context: Context) {
        val result = _analysisResult.value ?: return
        viewModelScope.launch {
            try {
                val intent = FileUtils.createShareIntent(context, result.collageBitmap)
                context.startActivity(android.content.Intent.createChooser(intent, "Share Collage"))
            }
            catch (e: Exception) {
                Toast.makeText(context, "Could not share", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun navigateToHome() {
        _processingState.value = ProcessingState.Idle
        _currentScreen.value = AppScreen.HOME
    }
}
