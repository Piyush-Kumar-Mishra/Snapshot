package com.example.snapshot.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.snapshot.ui.components.OverlappingAvatarsRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.snapshot.data.local.entity.VideoRecord
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
fun HistoryScreen(
    savedVideos: List<VideoRecord>,
    onHistoryItemClick: (VideoRecord) -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap any card to view, save, or share your collage.",
                    fontSize = 13.sp,
                    color = Gray500
                )
            }
        }

        if (savedVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(YellowCard)
                                .border(1.dp, YellowBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HistoryToggleOff,
                                contentDescription = null,
                                tint = Black900,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Saved Collages",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black900
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Collages from your analyzed videos will appear here.",
                            fontSize = 13.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToDashboard,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Black900,
                                contentColor = White
                            )
                        ) {
                            Text(
                                text = "Select a Video",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(savedVideos) { record ->
                HistoryItemCard(record = record, onClick = { onHistoryItemClick(record) })
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    record: VideoRecord,
    onClick: () -> Unit
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YellowCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${record.personCount} Unique People",
                    color = Black900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(YellowBadge)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${record.totalAppearances} appearances",
                        color = Black900,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            OverlappingAvatarsRow(
                avatars = avatarFiles,
                fallbackCount = record.personCount,
                maxCount = 5,
                avatarSize = 46.dp,
                overlapOffset = 16.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${record.durationMs / 1000}s video • $dateStr",
                        color = Gray500,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(White)
                        .border(0.8.dp, YellowBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "View",
                        tint = Black900,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
