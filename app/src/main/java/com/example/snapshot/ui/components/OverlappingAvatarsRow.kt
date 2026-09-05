package com.example.snapshot.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.snapshot.ui.theme.Black900
import com.example.snapshot.ui.theme.YellowBadge
import com.example.snapshot.ui.theme.YellowCard
import java.io.File

@Composable
fun OverlappingAvatarsRow(
    avatars: List<Any>,
    fallbackCount: Int = 0,
    maxCount: Int = 5,
    avatarSize: Dp = 46.dp,
    overlapOffset: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    val count = if (avatars.isNotEmpty()) minOf(avatars.size, maxCount) else minOf(fallbackCount, maxCount)
    if (count <= 0) return

    val totalWidth = avatarSize + (avatarSize - overlapOffset) * (count - 1)

    Box(
        modifier = modifier
            .width(totalWidth)
            .height(avatarSize)
    ) {
        for (index in 0 until count) {
            val item = if (index < avatars.size) avatars[index] else null

            Box(
                modifier = Modifier
                    .offset(x = (avatarSize - overlapOffset) * index)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(YellowCard)
                    .border(2.5.dp, Color.White, CircleShape)
            ) {
                when (item) {
                    is Bitmap -> {
                        Image(
                            bitmap = item.asImageBitmap(),
                            contentDescription = "Person ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    is String, is File -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Person ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(YellowBadge),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black900
                            )
                        }
                    }
                }
            }
        }
    }
}
