package com.micyou.ios.shared

import kotlinx.serialization.Serializable

/**
 * Audio configuration and status models shared between iOS modules.
 */

@Serializable
data class AudioConfig(
    val sampleRate: Int = 48000,
    val channels: Int = 1,
    val bitDepth: Int = 16,
    val bufferSizeMs: Int = 10
)

@Serializable
data class AudioStatus(
    val isCapturing: Boolean = false,
    val isConnected: Boolean = false,
    val sampleRate: Int = 48000,
    val channels: Int = 1,
    val bytesSent: Long = 0,
    val packetsSent: Long = 0,
    val lastError: String? = null
)

@Serializable
data class NetworkStats(
    val rttMs: Long = 0,
    val packetsLost: Long = 0,
    val jitterMs: Double = 0.0,
    val bandwidthKbps: Double = 0.0
)
