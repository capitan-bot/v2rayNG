package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.vm.MainViewModel

private val CardBackground = Color(0xFF1A1428)
private val CardBorder = Color(0xFF3B2A63)
private val BarActive = Color(0xFF22C55E)
private val BarBad = Color(0xFFEF4444)
private val BarOff = Color(0xFF4A4458)

/**
 * Card under the connect button: the selected server with its name, ping and signal bars.
 */
@Composable
fun SelectedServerCard(
    mainViewModel: MainViewModel,
    groupId: String,
    selectedGuid: String?,
    modifier: Modifier = Modifier
) {
    val groupStateFlow = remember(groupId) { mainViewModel.serverGroupState(groupId) }
    val groupState by groupStateFlow.collectAsStateWithLifecycle()
    val row = groupState.rows.firstOrNull { it.guid == selectedGuid }

    val delay = row?.testDelayMillis ?: 0L
    val activeBars = when {
        delay <= 0L -> 0
        delay <= 100L -> 4
        delay <= 250L -> 3
        delay <= 500L -> 2
        else -> 1
    }
    val barColor = when {
        delay < 0L -> BarBad
        delay == 0L -> BarOff
        else -> BarActive
    }
    val title = row?.remarks?.takeIf { it.isNotBlank() } ?: "-"
    val initial = title.firstOrNull()?.toString()?.uppercase() ?: "?"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF6D4BC4)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (delay > 0L) {
                Text(
                    text = stringResource(R.string.server_test_delay_value, delay),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = listOf(8.dp, 12.dp, 16.dp, 20.dp)
            heights.forEachIndexed { index, h ->
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(h)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < activeBars) barColor else BarOff)
                )
            }
        }
    }
}
