package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.vm.MainViewModel

private val CardBg = Color(0xFF1A1428)
private val CardBorder = Color(0xFF3B2A63)
private val Accent = Color(0xFFA855F7)
private val PingGood = Color(0xFF22C55E)
private val PingBad = Color(0xFFEF4444)

/**
 * All subscriptions one under the other. Each one is a card that can be opened and closed;
 * its servers are inside the card.
 */
@Composable
fun SubscriptionSections(
    groups: List<com.v2ray.ang.dto.GroupMapItem>,
    mainViewModel: MainViewModel,
    selectedGuid: String?,
    onSelectServer: (String, String) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
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
                    onMoreServer = onMoreServer
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
    onMoreServer: (String, ProfileItem) -> Unit
) {
    val stateFlow = remember(group.id) { mainViewModel.serverGroupState(group.id) }
    val groupState by stateFlow.collectAsStateWithLifecycle()
    var expanded by rememberSaveable(group.id) { mutableStateOf(startExpanded) }
    val title = group.remarks.ifBlank { "سرورها" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = "${groupState.rows.size} سرور",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = if (expanded) "\u25B2" else "\u25BC", color = Accent)
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
