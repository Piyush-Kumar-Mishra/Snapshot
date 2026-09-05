package com.example.snapshot.ui.processing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snapshot.model.ProcessingState
import com.example.snapshot.ui.theme.Black700
import com.example.snapshot.ui.theme.Black900
import com.example.snapshot.ui.theme.Gray300
import com.example.snapshot.ui.theme.Gray500
import com.example.snapshot.ui.theme.White
import com.example.snapshot.ui.theme.YellowBadge
import com.example.snapshot.ui.theme.YellowBorder
import com.example.snapshot.ui.theme.YellowCard

@Composable
fun ProcessingScreen(
    state: ProcessingState,
    onBackToHome: () -> Unit
) {
    BackHandler {
        onBackToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is ProcessingState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(YellowCard)
                            .border(1.2.dp, YellowBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Black900,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Processing Error",
                        color = Black900,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = state.message,
                        color = Black700,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onBackToHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black900,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Dashboard", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            else -> {
                val progressFloat = when (state) {
                    is ProcessingState.ExtractingFrames -> state.progress
                    is ProcessingState.DetectingFaces -> state.progress
                    is ProcessingState.GroupingPersons -> state.progress
                    is ProcessingState.GeneratingCollage -> 0.95f
                    is ProcessingState.Success -> 1.0f
                    else -> 0.04f
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = progressFloat,
                    label = "progress"
                )

                val statusText = when (state) {
                    is ProcessingState.ExtractingFrames -> "Reading video frames..."
                    is ProcessingState.DetectingFaces -> "Scanning for faces..."
                    is ProcessingState.GroupingPersons -> "Identifying unique people..."
                    is ProcessingState.GeneratingCollage -> "Creating your collage..."
                    is ProcessingState.Success -> "Collage complete!"
                    else -> "Getting ready..."
                }

                val detailText = when (state) {
                    is ProcessingState.ExtractingFrames ->
                        "Scanning frame ${state.currentFrame} of ${state.totalFrames}"
                    is ProcessingState.DetectingFaces ->
                        "${state.framesProcessed} frames analyzed"
                    is ProcessingState.GroupingPersons ->
                        "Found ${state.appearancesFound} appearances"
                    is ProcessingState.GeneratingCollage ->
                        "Selecting the clearest portrait shots"
                    else -> "Analyzing video..."
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = Black900,
                            trackColor = YellowCard,
                            strokeWidth = 7.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black900
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(YellowCard)
                            .border(1.dp, YellowBorder, RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp, horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = statusText,
                                color = Black900,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (detailText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = detailText,
                                    color = Black700,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Black900,
                        trackColor = YellowCard
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(YellowCard)
                            .border(1.dp, YellowBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StepRow("1. Scan video", animatedProgress >= 0.35f)
                        StepRow("2. Find people in video", animatedProgress >= 0.70f)
                        StepRow("3. Track each person", animatedProgress >= 0.85f)
                        StepRow("4. Generate collage", animatedProgress >= 0.95f)
                    }

                    OutlinedButton(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Black900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Black900)
                    ) {
                        Text(
                            text = "Cancel Analysis",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(title: String, isDone: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isDone) Black900 else White)
                .border(1.dp, if (isDone) Black900 else YellowBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Text(
            text = title,
            color = if (isDone) Black900 else Black700,
            fontSize = 13.sp,
            fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
