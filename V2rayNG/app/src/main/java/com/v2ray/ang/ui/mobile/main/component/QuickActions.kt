package com.v2ray.ang.ui.mobile.main.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.ui.mobile.main.MainAction

private val TileBg = Color(0xFF1A1428)
private val TileBorder = Color(0xFF3B2A63)
private val TileIcon = Color(0xFFA855F7)

private fun DrawScope.qrSquare(color: Color, x: Float, y: Float, u: Float) {
    drawRect(
        color = color,
        topLeft = Offset(x * u + u * 0.45f, y * u + u * 0.45f),
        size = Size(u * 2.2f, u * 2.2f),
        style = Stroke(width = u * 0.8f)
    )
}

@Composable
private fun QrGlyph(color: Color) {
    Canvas(Modifier.size(28.dp)) {
        val u = size.minDimension / 7f
        qrSquare(color, 0f, 0f, u)
        qrSquare(color, 4f, 0f, u)
        qrSquare(color, 0f, 4f, u)
        drawRect(color, Offset(4.4f * u, 4.4f * u), Size(u, u))
        drawRect(color, Offset(5.8f * u, 5.8f * u), Size(u, u))
        drawRect(color, Offset(5.8f * u, 4.4f * u), Size(u, u))
    }
}

@Composable
private fun Tile(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .border(1.dp, TileBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFFC4B5FD), maxLines = 1)
    }
}

/**
 * Four quick buttons under the server card.
 */
@Composable
fun QuickActions(
    selectedGuid: String?,
    onAction: (MainAction) -> Unit,
    onListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Tile("تست پینگ", Modifier.weight(1f), { onAction(MainAction.TestCurrentServer) }) {
                Text(text = "\u26A1", fontSize = 26.sp, color = TileIcon)
            }
            Tile("لیست سرورها", Modifier.weight(1f), onListClick) {
                Icon(painterResource(R.drawable.ic_subscriptions_24dp), null, tint = TileIcon, modifier = Modifier.size(28.dp))
            }
            Tile("اسکن QR", Modifier.weight(1f), { onAction(MainAction.ImportQRcode) }) {
                QrGlyph(TileIcon)
            }
            Tile("اشتراک\u200Cگذاری", Modifier.weight(1f), {
                selectedGuid?.let { onAction(MainAction.ShareClipboard(it)) }
            }) {
                Icon(painterResource(R.drawable.ic_share_24dp), null, tint = TileIcon, modifier = Modifier.size(28.dp))
            }
        }
    }
}
