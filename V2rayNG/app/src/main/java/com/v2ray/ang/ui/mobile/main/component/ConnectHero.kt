package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.mobile.main.MainAction
import java.util.Locale
import kotlinx.coroutines.delay

private val NeonGreen = Color(0xFF22FF88)
private val IdlePurple = Color(0xFF8B5CF6)
private val ButtonBg = Color(0xFF140D26)

private fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}

@Composable
private fun PowerGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = size.minDimension * 0.10f
        val r = size.minDimension / 2f - stroke
        val c = center
        drawArc(
            color = color,
            startAngle = -50f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(c.x - r, c.y - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = color,
            start = Offset(c.x, c.y - r * 1.05f),
            end = Offset(c.x, c.y - r * 0.1f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Hero: eagle picture behind a big round connect button.
 * Idle: purple ring, power icon, "connect" text. Connected: neon green ring, a square, the text and a timer.
 */
@Composable
fun ConnectHero(
    displayText: String,
    isRunning: Boolean,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isRunning) NeonGreen else IdlePurple
    val bg = MaterialTheme.colorScheme.background

    var startMillis by rememberSaveable { mutableStateOf(0L) }
    var elapsedSeconds by rememberSaveable { mutableStateOf(0L) }
    LaunchedEffect(isRunning) {
        if (isRunning) {
            if (startMillis == 0L) startMillis = System.currentTimeMillis()
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startMillis) / 1000
                delay(1000)
            }
        } else {
            startMillis = 0L
            elapsedSeconds = 0L
        }
    }

    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha = if (isRunning) pulse else 0.35f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ringColor.copy(alpha = glowAlpha),
                                    ringColor.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .border(1.dp, ringColor.copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(
                            elevation = if (isRunning) 24.dp else 8.dp,
                            shape = CircleShape,
                            ambientColor = ringColor,
                            spotColor = ringColor
                        )
                        .clip(CircleShape)
                        .background(ButtonBg)
                        .border(width = 5.dp, color = ringColor, shape = CircleShape)
                        .clickable { onAction(MainAction.ToggleService) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRunning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonGreen)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "متصل شد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatDuration(elapsedSeconds),
                                style = MaterialTheme.typography.bodyLarge,
                                color = NeonGreen
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PowerGlyph(color = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "اتصال",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "برای اتصال کلیک کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB9A8E8)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}
