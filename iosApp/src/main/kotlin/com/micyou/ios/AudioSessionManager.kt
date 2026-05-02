package com.micyou.ios

import com.micyou.ios.shared.AudioConfig
import com.micyou.ios.shared.AudioStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUUID

/**
 * Manages iOS audio capture session.
 *
 * Bridges between the Objective-C audio capture (MicYouAudioBridge)
 * and the Kotlin transport layer.
 *
 * Uses fixed ports matching the ios2pc-myp plugin:
 * - TCP Control: 16000
 * - UDP Audio: 16001
 */
class AudioSessionManager {

    private val nativeBridge = MicYouAudioBridge()
    private var transport: TransportClient? = null

    private val _audioStatus = MutableStateFlow(AudioStatus())
    val audioStatus: StateFlow<AudioStatus> = _audioStatus.asStateFlow()

    private val deviceId = NSUUID.UUID().UUIDString()
    private var deviceName = "iPhone"
    private var audioConfig = AudioConfig()

    init {
        nativeBridge.setFrameCallback { pcmData, sampleRate, channels ->
            onAudioFrame(pcmData, sampleRate, channels)
        }
    }

    /**
     * Prepare audio session.
     * @return Status message.
     */
    fun prepare(): String {
        val result = nativeBridge.prepareAudio()

        val sampleRateMatch = Regex("""(\d+)Hz""").find(result)
        val channelsMatch = Regex("""(\d+)ch""").find(result)

        audioConfig = AudioConfig(
            sampleRate = sampleRateMatch?.groupValues?.get(1)?.toIntOrNull() ?: 48000,
            channels = channelsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
        )

        _audioStatus.value = _audioStatus.value.copy(
            sampleRate = audioConfig.sampleRate,
            channels = audioConfig.channels
        )

        return result
    }

    /**
     * Attach network transport using fixed ports.
     * @param host PC IP address
     */
    fun attachTransport(host: String): String {
        val client = TransportClient()
        transport = client

        val success = client.connect(
            host = host,
            deviceId = deviceId,
            deviceName = deviceName,
            sampleRate = audioConfig.sampleRate,
            channels = audioConfig.channels
        )

        return if (success) {
            _audioStatus.value = _audioStatus.value.copy(isConnected = true)
            "Connecting to $host (control: ${TransportClient.DEFAULT_CONTROL_PORT}, audio: ${TransportClient.DEFAULT_AUDIO_PORT})..."
        } else {
            "Connection failed"
        }
    }

    /**
     * Start audio capture and streaming.
     */
    fun startCapture(): String {
        val result = nativeBridge.startCapture()

        if (result.contains("started", ignoreCase = true)) {
            _audioStatus.value = _audioStatus.value.copy(isCapturing = true)
        }

        return result
    }

    /**
     * Stop audio capture.
     */
    fun stopCapture(): String {
        val result = nativeBridge.stopCapture()

        _audioStatus.value = _audioStatus.value.copy(isCapturing = false)

        return result
    }

    /**
     * Disconnect and cleanup.
     */
    fun disconnect() {
        stopCapture()
        transport?.disconnect()
        transport = null

        _audioStatus.value = AudioStatus()
    }

    /**
     * Get current connection state.
     */
    fun getConnectionState(): TransportClient.ConnectionState {
        return transport?.connectionState?.value ?: TransportClient.ConnectionState.DISCONNECTED
    }

    // --- Private ---

    private fun onAudioFrame(pcmData: ByteArray, sampleRate: Int, channels: Int) {
        transport?.sendAudio(pcmData, sampleRate, channels)

        _audioStatus.value = _audioStatus.value.copy(
            bytesSent = _audioStatus.value.bytesSent + pcmData.size,
            packetsSent = _audioStatus.value.packetsSent + 1
        )
    }
}
