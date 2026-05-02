package com.micyou.ios

import com.micyou.ios.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Network transport client for iOS to PC communication.
 *
 * Connects to the ios2pc-myp plugin using FIXED ports:
 * - TCP Control: 16000
 * - UDP Audio: 16001
 *
 * Single-device mode: iOS app connects to one PC at a time.
 *
 * NOTE: This is the common interface. Platform-specific implementation
 * is in iosMain/TransportClient.ios.kt using platform.posix APIs.
 */
expect class TransportClient() {

    val connectionState: StateFlow<ConnectionState>
    val stats: StateFlow<NetworkStats>

    // Fixed ports matching the ios2pc-myp plugin
    companion object {
        val DEFAULT_CONTROL_PORT: Int
        val DEFAULT_AUDIO_PORT: Int
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        HANDSHAKING,
        CONNECTED,
        STREAMING,
        ERROR,
        REJECTED
    }

    fun connect(
        host: String,
        deviceId: String,
        deviceName: String,
        sampleRate: Int,
        channels: Int
    ): Boolean

    fun sendAudio(pcmData: ByteArray, sampleRate: Int, channels: Int)

    fun disconnect()
}
