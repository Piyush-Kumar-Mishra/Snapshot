package com.example.snapshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snapshot.ui.AppScreen
import com.example.snapshot.ui.BottomTab
import com.example.snapshot.ui.MainScreen
import com.example.snapshot.ui.MainViewModel
import com.example.snapshot.ui.processing.ProcessingScreen
import com.example.snapshot.ui.result.ResultScreen
import com.example.snapshot.ui.theme.Black900
import com.example.snapshot.ui.theme.SnapshotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapshotTheme {
                val viewModel: MainViewModel = viewModel()
                SnapshotApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SnapshotApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val processingState by viewModel.processingState.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val savedVideos by viewModel.savedVideos.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(onTimeout = { viewModel.onSplashDone() })
            }

            AppScreen.HOME -> {
                MainScreen(
                    selectedTab = selectedTab,
                    processingState = processingState,
                    analysisResult = analysisResult,
                    savedVideos = savedVideos,
                    onSelectTab = { tab -> viewModel.selectTab(tab) },
                    onVideoSelected = { uri -> viewModel.onVideoSelected(uri) },
                    onViewResult = { result -> viewModel.viewResult(result) },
                    onHistoryItemClick = { record -> viewModel.loadPreviousAnalysis(record) },
                    onCancelProcessing = { viewModel.cancelProcessing() },
                    onDismissProcessing = { viewModel.dismissProcessing() }
                )
            }

            AppScreen.PROCESSING -> {
                ProcessingScreen(
                    state = processingState,
                    onBackToHome = { viewModel.navigateToHome() }
                )
            }

            AppScreen.RESULT -> {
                val result = analysisResult
                if (result != null) {
                    ResultScreen(
                        result = result,
                        onSaveToGallery = { viewModel.saveCollageToGallery(context) },
                        onShareCollage = { viewModel.shareCollage(context) },
                        onBackHome = { viewModel.navigateToHome() }
                    )
                } else {
                    viewModel.navigateToHome()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Snapshot",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Black900
        )
    }
}