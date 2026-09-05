package com.example.snapshot.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.snapshot.ui.components.OverlappingAvatarsRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.snapshot.data.local.entity.VideoRecord
import com.example.snapshot.model.ProcessingState
import com.example.snapshot.model.VideoAnalysisResult
import com.example.snapshot.ui.theme.Black700
import com.example.snapshot.ui.theme.Black900
import com.example.snapshot.ui.theme.Gray500
import com.example.snapshot.ui.theme.White
import com.example.snapshot.ui.theme.YellowBadge
import com.example.snapshot.ui.theme.YellowBorder
import com.example.snapshot.ui.theme.YellowCard
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    processingState: ProcessingState,
    analysisResult: VideoAnalysisResult?,
    latestSavedVideo: VideoRecord?,
    onVideoSelected: (Uri) -> Unit,
    onViewResult: (VideoAnalysisResult) -> Unit,
    onViewHistoryRecord: (VideoRecord) -> Unit,
    onCancelProcessing: () -> Unit,
    onDismissProcessing: () -> Unit
) {
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onVideoSelected(uri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(YellowCard)
                        .border(1.2.dp, YellowBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Snapshot Camera",
                        tint = Black900,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "S",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Black900
                    )
                    Text(
                        text = "napshot",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black900,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(YellowCard)
                    .border(
                        width = 1.2.dp,
                        color = YellowBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { videoPickerLauncher.launch("video/*") }
                    .padding(vertical = 26.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(White)
                            .border(1.dp, YellowBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Select Video",
                            tint = Black900,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black900,
                            contentColor = White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Choose Video",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent & Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black900,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            when (processingState) {
                is ProcessingState.ExtractingFrames,
                is ProcessingState.DetectingFaces,
                is ProcessingState.GroupingPersons,
                is ProcessingState.GeneratingCollage -> {
                    ActiveBackgroundProcessingCard(
                        state = processingState,
                        onCancel = onCancelProcessing
                    )
                }

                is ProcessingState.Success -> {
                    val result = processingState.result
                    CollageSuccessCard(
                        result = result,
                        onViewCollage = { onViewResult(result) },
                        onDismiss = onDismissProcessing
                    )
                }

                is ProcessingState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, YellowBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = YellowCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Black900,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Processing Failed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Black900
                                )
                                Text(
                                    text = processingState.message,
                                    fontSize = 12.sp,
                                    color = Black700
                                )
                            }
                            TextButton(onClick = onDismissProcessing) {
                                Text("Dismiss", color = Black900, fontSize = 12.sp)
                            }
                        }
                    }
                }

                is ProcessingState.Idle -> {
                    if (latestSavedVideo != null) {
                        LatestSavedCollageCard(
                            record = latestSavedVideo,
                            onViewCollage = { onViewHistoryRecord(latestSavedVideo) }
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, YellowBorder, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = YellowCard),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(White)
                                        .border(0.8.dp, YellowBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Sync,
                                        contentDescription = null,
                                        tint = Black900,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ready to Create",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Black900
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Select a video above to find everyone who appears in it.",
                                        fontSize = 12.sp,
                                        color = Black700,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBackgroundProcessingCard(
    state: ProcessingState,
    onCancel: () -> Unit
) {
    val progressFloat = when (state) {
        is ProcessingState.ExtractingFrames -> state.progress
        is ProcessingState.DetectingFaces -> state.progress
        is ProcessingState.GroupingPersons -> state.progress
        is ProcessingState.GeneratingCollage -> 0.95f
        else -> 0.05f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFloat,
        label = "bgProgress"
    )

    val statusTitle = when (state) {
        is ProcessingState.ExtractingFrames -> "Reading video frames..."
        is ProcessingState.DetectingFaces -> "Scanning for faces..."
        is ProcessingState.GroupingPersons -> "Identifying unique people..."
        is ProcessingState.GeneratingCollage -> "Creating your collage..."
        else -> "Processing..."
    }

    val statusSubtitle = when (state) {
        is ProcessingState.ExtractingFrames ->
            "Scanning video (${state.currentFrame} of ${state.totalFrames})"
        is ProcessingState.DetectingFaces ->
            "${state.framesProcessed} frames analyzed"
        is ProcessingState.GroupingPersons ->
            "Found ${state.appearancesFound} appearances"
        is ProcessingState.GeneratingCollage ->
            "Selecting best shots for collage"
        else -> "Analyzing video..."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, YellowBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YellowCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Black900,
                        trackColor = White,
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "PROCESSING...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black900,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black900
                )
            }

            Column {
                Text(
                    text = statusTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusSubtitle,
                    fontSize = 12.sp,
                    color = Black700
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Black900,
                trackColor = White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Running in background",
                    fontSize = 11.sp,
                    color = Gray500
                )

                TextButton(
                    onClick = onCancel,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Cancel", color = Black900, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun CollageSuccessCard(
    result: VideoAnalysisResult,
    onViewCollage: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, YellowBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YellowCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Black900,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Analysis Complete!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Black900
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(YellowBadge)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${result.persons.size} Unique People",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black900
                    )
                }
            }

            val faceShots = result.persons.map { it.representativeShot }
            OverlappingAvatarsRow(
                avatars = faceShots,
                fallbackCount = result.persons.size,
                maxCount = 5,
                avatarSize = 50.dp,
                overlapOffset = 18.dp
            )

            Text(
                text = "${result.totalAppearances} total appearances across video",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Black700
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewCollage,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Black900,
                        contentColor = White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View Collage",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("Dismiss", color = Gray500, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LatestSavedCollageCard(
    record: VideoRecord,
    onViewCollage: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        .format(Date(record.processedTimestamp))

    val avatarFiles = remember(record.id) {
        val dir = context.filesDir
        val matching = dir.listFiles { f -> f.name.startsWith("person_${record.id}_") }
        matching?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, YellowBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onViewCollage),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YellowCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LATEST ANALYSIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black900,
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(YellowBadge)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${record.personCount} People",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black900
                    )
                }
            }

            OverlappingAvatarsRow(
                avatars = avatarFiles,
                fallbackCount = record.personCount,
                maxCount = 5,
                avatarSize = 50.dp,
                overlapOffset = 18.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${record.totalAppearances} appearances · ${record.durationMs / 1000}s video",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Black900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Gray500
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "View Collage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black900
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Black900,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
