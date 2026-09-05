package com.example.snapshot.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snapshot.data.local.entity.VideoRecord
import com.example.snapshot.ui.dashboard.DashboardScreen
import com.example.snapshot.ui.history.HistoryScreen
import com.example.snapshot.ui.theme.Black900
import com.example.snapshot.ui.theme.Gray300
import com.example.snapshot.ui.theme.Gray500

import com.example.snapshot.model.ProcessingState
import com.example.snapshot.model.VideoAnalysisResult

@Composable
fun MainScreen(
    selectedTab: BottomTab,
    processingState: ProcessingState,
    analysisResult: VideoAnalysisResult?,
    savedVideos: List<VideoRecord>,
    onSelectTab: (BottomTab) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onViewResult: (VideoAnalysisResult) -> Unit,
    onHistoryItemClick: (VideoRecord) -> Unit,
    onCancelProcessing: () -> Unit,
    onDismissProcessing: () -> Unit
) {
    // Intercept system back button: if on History tab, go back to Dashboard instead of exiting
    BackHandler(enabled = selectedTab != BottomTab.DASHBOARD) {
        onSelectTab(BottomTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = Gray300, thickness = 0.8.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CompactNavItem(
                        title = "Dashboard",
                        icon = Icons.Outlined.Dashboard,
                        selectedIcon = Icons.Filled.Dashboard,
                        isSelected = selectedTab == BottomTab.DASHBOARD,
                        onClick = { onSelectTab(BottomTab.DASHBOARD) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactNavItem(
                        title = "History",
                        icon = Icons.Outlined.History,
                        selectedIcon = Icons.Filled.History,
                        isSelected = selectedTab == BottomTab.HISTORY,
                        onClick = { onSelectTab(BottomTab.HISTORY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomTab.DASHBOARD -> {
                    DashboardScreen(
                        processingState = processingState,
                        analysisResult = analysisResult,
                        latestSavedVideo = savedVideos.firstOrNull(),
                        onVideoSelected = onVideoSelected,
                        onViewResult = onViewResult,
                        onViewHistoryRecord = onHistoryItemClick,
                        onCancelProcessing = onCancelProcessing,
                        onDismissProcessing = onDismissProcessing
                    )
                }

                BottomTab.HISTORY -> {
                    HistoryScreen(
                        savedVideos = savedVideos,
                        onHistoryItemClick = onHistoryItemClick,
                        onNavigateToDashboard = { onSelectTab(BottomTab.DASHBOARD) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactNavItem(
    title: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = title,
            tint = if (isSelected) Black900 else Gray500,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Black900 else Gray500
        )
    }
}
