package com.v2ray.ang.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrafficSnapshot(
    val upSpeed: Long = 0L,
    val downSpeed: Long = 0L,
    val upTotal: Long = 0L,
    val downTotal: Long = 0L,
    val upHistory: List<Float> = emptyList(),
    val downHistory: List<Float> = emptyList()
)

/**
 * Live traffic numbers for the UI. The core service runs in another process, so it sends a
 * small broadcast once a second and the UI process collects it here.
 */
object TrafficMonitor {
    private const val ACTION = "com.v2ray.ang.TRAFFIC_STATS"
    private const val MAX_POINTS = 40
    private val _state = MutableStateFlow(TrafficSnapshot())
    val state: StateFlow<TrafficSnapshot> = _state.asStateFlow()

    /** Called in the service process. */
    fun send(ctx: Context?, upBytes: Long, downBytes: Long, seconds: Double) {
        if (ctx == null) return
        try {
            ctx.sendBroadcast(
                Intent(ACTION).setPackage(ctx.packageName)
                    .putExtra("up", upBytes)
                    .putExtra("down", downBytes)
                    .putExtra("sec", seconds)
            )
        } catch (_: Exception) {
        }
    }

    /** Called in the UI process. */
    fun register(ctx: Context): BroadcastReceiver {
        val app = ctx.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                publish(
                    intent.getLongExtra("up", 0L),
                    intent.getLongExtra("down", 0L),
                    intent.getDoubleExtra("sec", 1.0)
                )
            }
        }
        ContextCompat.registerReceiver(app, receiver, IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        return receiver
    }

    fun unregister(ctx: Context, receiver: BroadcastReceiver) {
        try {
            ctx.applicationContext.unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
    }

    fun publish(upBytes: Long, downBytes: Long, seconds: Double) {
        val s = if (seconds > 0.0) seconds else 1.0
        val up = (upBytes / s).toLong()
        val down = (downBytes / s).toLong()
        _state.update { cur ->
            TrafficSnapshot(
                upSpeed = up,
                downSpeed = down,
                upTotal = cur.upTotal + upBytes,
                downTotal = cur.downTotal + downBytes,
                upHistory = (cur.upHistory + up.toFloat()).takeLast(MAX_POINTS),
                downHistory = (cur.downHistory + down.toFloat()).takeLast(MAX_POINTS)
            )
        }
    }

    fun reset() {
        _state.value = TrafficSnapshot()
    }
}
