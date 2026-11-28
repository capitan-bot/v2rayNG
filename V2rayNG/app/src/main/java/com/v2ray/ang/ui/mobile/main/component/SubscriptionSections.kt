package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.mobile.main.MainAction
import com.v2ray.ang.util.Utils
import com.v2ray.ang.vm.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CardTop = Color(0xFF231640)
private val CardBottom = Color(0xFF150E27)
private val CardBorder = Color(0xFF3F2C6E)
private val Accent = Color(0xFFA855F7)
private val AccentSoft = Color(0xFFC4B5FD)
private val BarBg = Color(0xFF0F0A1E)
private val BarFill1 = Color(0xFF6D4BC4)
private val BarWarn1 = Color(0xFFB91C1C)
private val BarWarn2 = Color(0xFFEF4444)
private val PingGood = Color(0xFF22C55E)
private val PingBad = Color(0xFFEF4444)

private fun fmtBytes(b: Long): String {
    val gb = b / 1073741824.0
    return if (gb >= 1.0) String.format(Locale.US, "%.1fGB", gb)
    else String.format(Locale.US, "%.0fMB", b / 1048576.0)
}

private fun fmtDate(seconds: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.US).format(Date(seconds * 1000))

@Composable
private fun RefreshGlyph(color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val stroke = size.minDimension * 0.11f
        val r = size.minDimension / 2f - stroke * 1.8f
        val c = center
        drawArc(
            color = color, startAngle = 40f, sweepAngle = 280f, useCenter = false,
            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        val a = 320f * (PI.toFloat() / 180f)
        val px = c.x + r * cos(a)
        val py = c.y + r * sin(a)
        val tx = -sin(a)
        val ty = cos(a)
        val nx = cos(a)
        val ny = sin(a)
        val s = stroke * 2.2f
        val path = Path().apply {
            moveTo(px + tx * s, py + ty * s)
            lineTo(px + nx * s * 0.9f, py + ny * s * 0.9f)
            lineTo(px - nx * s * 0.9f, py - ny * s * 0.9f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun GaugeGlyph(color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val stroke = size.minDimension * 0.10f
        val r = size.minDimension / 2f - stroke * 1.2f
        val c = center
        drawArc(
            color = color, startAngle = 150f, sweepAngle = 240f, useCenter = false,
            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        val a = 300f * (PI.toFloat() / 180f)
        drawLine(
            color = color, start = c,
            end = Offset(c.x + r * 0.8f * cos(a), c.y + r * 0.8f * sin(a)),
            strokeWidth = stroke, cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = stroke * 0.9f, center = c)
    }
}

/**
 * All subscriptions one under the other. Each one is a card that can be opened and closed;
 * its servers are inside the card.
 */
@Composable
fun SubscriptionSections(
    groups: List<GroupMapItem>,
    mainViewModel: MainViewModel,
    selectedGuid: String?,
    onSelectServer: (String, String) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        groups.forEachIndexed { index, group ->
            key(group.id) {
                SubscriptionCard(
                    group = group,
                    startExpanded = index == 0,
                    mainViewModel = mainViewModel,
                    selectedGuid = selectedGuid,
                    onSelectServer = onSelectServer,
                    onMoreServer = onMoreServer,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    group: GroupMapItem,
    startExpanded: Boolean,
    mainViewModel: MainViewModel,
    selectedGuid: String?,
    onSelectServer: (String, String) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onAction: (MainAction) -> Unit
) {
    val stateFlow = remember(group.id) { mainViewModel.serverGroupState(group.id) }
    val groupState by stateFlow.collectAsStateWithLifecycle()
    var expanded by rememberSaveable(group.id) { mutableStateOf(startExpanded) }
    val title = group.remarks.ifBlank { "سرورها" }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val sub = remember(group.id, groupState) { MmkvManager.decodeSubscription(group.id) }
    val info = sub?.usageInfo
    val canUpdate = sub != null && sub.url.isNotEmpty()
    val lastUpdated = if (sub != null && sub.lastUpdated > 0L)
        Utils.formatTimestamp(sub.lastUpdated, "dd.MM.yyyy HH:mm", Locale.US) else ""
    val shape = RoundedCornerShape(18.dp)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(CardTop, CardBottom)))
                .border(1.dp, CardBorder, shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (expanded) "\u25BE" else "\u25B8", color = Accent, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lastUpdated.isNotEmpty()) "$lastUpdated  |  ${groupState.rows.size} سرور"
                        else "${groupState.rows.size} سرور",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (canUpdate) {
                    IconButton(
                        onClick = { onAction(MainAction.UpdateGroupSubscription(group.id)) },
                        modifier = Modifier.size(40.dp)
                    ) { RefreshGlyph(AccentSoft) }
                }
                IconButton(
                    onClick = {
                        expanded = true
                        scope.launch {
                            onAction(MainAction.SelectGroup(group.id))
                            delay(400)
                            onAction(MainAction.TestAllServers)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) { GaugeGlyph(AccentSoft) }
            }

            if (info != null && (info.total > 0L || info.used > 0L || info.expire > 0L)) {
                val frac = if (info.total > 0L) (info.used.toFloat() / info.total).coerceIn(0f, 1f) else 0f
                val warn = frac > 0.9f
                val expired = info.expire > 0L && info.expire * 1000 < System.currentTimeMillis()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 12.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(1.5.dp, AccentSoft, CircleShape)
                            .clickable(enabled = info.webPageUrl != null) {
                                info.webPageUrl?.let { runCatching { uriHandler.openUri(it) } }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "i", color = AccentSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BarBg)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .fillMaxWidth(frac)
                                .background(
                                    Brush.horizontalGradient(
                                        if (warn) listOf(BarWarn1, BarWarn2) else listOf(BarFill1, Accent)
                                    )
                                )
                        )
                        Text(
                            text = if (info.total > 0L) "${fmtBytes(info.used)}/${fmtBytes(info.total)}"
                            else "${fmtBytes(info.used)} / نامحدود",
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (info.expire > 0L) fmtDate(info.expire) else "نامحدود",
                        color = if (expired) PingBad else AccentSoft,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    if (info.supportUrl != null) {
                        IconButton(
                            onClick = { info.supportUrl?.let { runCatching { uriHandler.openUri(it) } } },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share_24dp),
                                contentDescription = null,
                                tint = AccentSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (expanded) {
                groupState.rows.forEach { row ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorder)
                    )
                    val isSelected = row.guid == selectedGuid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Accent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onSelectServer(group.id, row.guid) }
                            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = row.remarks,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = row.typeDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (row.testDelayMillis > 0L) {
                            Text(
                                text = stringResource(R.string.server_test_delay_value, row.testDelayMillis),
                                style = MaterialTheme.typography.bodySmall,
                                color = PingGood
                            )
                        } else if (row.testDelayMillis < 0L) {
                            Text(
                                text = "\u2717",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PingBad
                            )
                        }
                        IconButton(
                            onClick = { onMoreServer(row.guid, row.profile) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert_24dp),
                                contentDescription = stringResource(R.string.acc_more),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
