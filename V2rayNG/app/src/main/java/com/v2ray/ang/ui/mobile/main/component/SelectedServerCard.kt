package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.vm.MainViewModel

private val CardBackground = Color(0xFF1A1428)
private val CardBorder = Color(0xFF3B2A63)
private val StatusOn = Color(0xFF22FF88)
private val StatusOff = Color(0xFFA855F7)

/**
 * Card under the connect button: selected server on one side, connection status on the other.
 */
@Composable
fun SelectedServerCard(
    mainViewModel: MainViewModel,
    groupId: String,
    selectedGuid: String?,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val groupStateFlow = remember(groupId) { mainViewModel.serverGroupState(groupId) }
    val groupState by groupStateFlow.collectAsStateWithLifecycle()
    val row = groupState.rows.firstOrNull { it.guid == selectedGuid }

    val delay = row?.testDelayMillis ?: 0L
    val title = row?.remarks?.takeIf { it.isNotBlank() } ?: "-"
    val initial = title.firstOrNull()?.toString()?.uppercase() ?: "?"
    val statusColor = if (isRunning) StatusOn else StatusOff
    val statusText = if (isRunning) "متصل است" else "قطع است"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6D4BC4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initial, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "سرور انتخاب شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (delay > 0L) {
                        Text(
                            text = stringResource(R.string.server_test_delay_value, delay),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(CardBorder)
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "وضعیت",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }
        }
    }
}
