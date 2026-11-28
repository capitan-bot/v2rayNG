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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.handler.TrafficMonitor
import com.v2ray.ang.handler.TrafficSnapshot
import com.v2ray.ang.ui.mobile.main.MainAction
import java.util.Locale
import kotlinx.coroutines.delay

private val NeonGreen = Color(0xFF22FF88)
private val ConnectedBlue = Color(0xFF6D7CFF)
private val IdlePurple = Color(0xFF8B5CF6)
private val IdlePurpleLight = Color(0xFFC4A6FF)
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

@Composable
private fun CheckGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = size.minDimension * 0.14f
        drawLine(color, Offset(w * 0.16f, h * 0.54f), Offset(w * 0.42f, h * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.42f, h * 0.78f), Offset(w * 0.86f, h * 0.24f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

private val DownColor = Color(0xFF38BDF8)
private val UpColor = Color(0xFFA855F7)
private const val CHART_POINTS = 40

private fun fmtSize(b: Long): String = when {
    b >= 1_073_741_824L -> String.format(Locale.US, "%.2f GB", b / 1_073_741_824.0)
    b >= 1_048_576L -> String.format(Locale.US, "%.1f MB", b / 1_048_576.0)
    b >= 1024L -> String.format(Locale.US, "%.0f KB", b / 1024.0)
    else -> b.toString() + " B"
}

private fun padPoints(l: List<Float>): List<Float> =
    List((CHART_POINTS - l.size).coerceAtLeast(0)) { 0f } + l

private fun DrawScope.curve(points: List<Float>, maxV: Float, fill: Boolean): Path {
    val path = Path()
    if (points.size < 2) return path
    val w = size.width
    val h = size.height
    val step = w / (points.size - 1)
    val y = { v: Float -> h * 0.95f - (v / maxV).coerceIn(0f, 1f) * h * 0.85f }
    path.moveTo(0f, y(points[0]))
    for (i in 1 until points.size) {
        val x0 = (i - 1) * step
        val x1 = i * step
        val cx = (x0 + x1) / 2f
        path.cubicTo(cx, y(points[i - 1]), cx, y(points[i]), x1, y(points[i]))
    }
    if (fill) {
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()
    }
    return path
}

@Composable
private fun StatCard(
    arrow: String,
    color: Color,
    label: String,
    total: Long,
    speed: Long,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF231640), Color(0xFF150E27))))
            .border(1.dp, Color(0xFF3F2C6E), shape)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = arrow + " " + fmtSize(total), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = fmtSize(speed) + "/s", color = color, fontSize = 12.sp, maxLines = 1)
        Text(text = label, color = Color(0xFFB9A8E8), fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun TrafficPanel(t: TrafficSnapshot, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(shape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF1B1233), Color(0xFF110A22))))
                    .border(1.dp, Color(0xFF3F2C6E), shape)
            ) {
                val down = padPoints(t.downHistory)
                val up = padPoints(t.upHistory)
                val maxV = maxOf(20_480f, (down + up).maxOrNull() ?: 0f)
                for (i in 1..3) {
                    val gy = size.height * i / 4f
                    drawLine(Color(0x22FFFFFF), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                }
                val line = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawPath(curve(down, maxV, true), Brush.verticalGradient(listOf(DownColor.copy(alpha = 0.35f), Color.Transparent)))
                drawPath(curve(down, maxV, false), DownColor, style = line)
                drawPath(curve(up, maxV, true), Brush.verticalGradient(listOf(UpColor.copy(alpha = 0.35f), Color.Transparent)))
                drawPath(curve(up, maxV, false), UpColor, style = line)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("\u2193", DownColor, "دریافت", t.downTotal, t.downSpeed, Modifier.weight(1f))
                StatCard("\u2191", UpColor, "ارسال", t.upTotal, t.upSpeed, Modifier.weight(1f))
            }
        }
    }
}

/**
 * Hero: smaller eagle on top, globe below, round connect button over the globe.
 * Idle: purple ring, power icon, "connect" text. Connected: neon green ring, a square, the text and a timer.
 */
@Composable
fun ConnectHero(
    displayText: String,
    isRunning: Boolean,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isRunning) ConnectedBlue else IdlePurple
    val ringLight = if (isRunning) Color(0xFF38BDF8) else IdlePurpleLight
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
            TrafficMonitor.reset()
        }
    }

    val ctx = LocalContext.current
    DisposableEffect(Unit) {
        val receiver = TrafficMonitor.register(ctx)
        onDispose { TrafficMonitor.unregister(ctx, receiver) }
    }
    val traffic by TrafficMonitor.state.collectAsStateWithLifecycle()
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
                .height(if (isRunning) 480.dp else 400.dp)
        ) {
            if (isRunning) {
                TrafficPanel(traffic, Modifier.align(Alignment.TopCenter))
            } else {
            Image(
                painter = painterResource(R.drawable.eagle_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to bg,
                            0.25f to Color.Transparent,
                            0.65f to Color.Transparent,
                            1f to bg
                        )
                    )
            )
            }
            Image(
                painter = painterResource(R.drawable.globe_hero),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
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
                        .size(184.dp)
                        .border(1.dp, ringColor.copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .shadow(
                            elevation = if (isRunning) 24.dp else 8.dp,
                            shape = CircleShape,
                            ambientColor = ringColor,
                            spotColor = ringColor
                        )
                        .clip(CircleShape)
                        .background(ButtonBg)
                        .border(
                            width = 4.dp,
                            brush = if (isRunning) Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6), Color(0xFFC026D3), Color(0xFF38BDF8))) else Brush.verticalGradient(listOf(ringLight, ringColor)),
                            shape = CircleShape
                        )
                        .clickable { onAction(MainAction.ToggleService) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRunning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CheckGlyph(color = NeonGreen, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "متصل شد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = formatDuration(elapsedSeconds),
                                style = MaterialTheme.typography.bodyLarge,
                                color = NeonGreen
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PowerGlyph(color = Color.White, modifier = Modifier.size(52.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "اتصال",
                                style = MaterialTheme.typography.titleMedium,
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
