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
 */
class TransportClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var controlSocket: Int = -1
    private var controlConnected = false

    private var audioSocket: Int = -1
    private var audioConnected = false

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(NetworkStats())
    val stats: StateFlow<NetworkStats> = _stats.asStateFlow()

    private var keepAliveJob: Job? = null
    private var audioSequence = 0L
    private var streamId = ""

    private val audioQueue = Channel<ByteArray>(Channel.BUFFERED)

    companion object {
        const val DEFAULT_CONTROL_PORT = 16000
        const val DEFAULT_AUDIO_PORT = 16001
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
    ): Boolean {
        if (_connectionState.value != ConnectionState.DISCONNECTED) {
            return false
        }

        _connectionState.value = ConnectionState.CONNECTING
        streamId = deviceId

        scope.launch {
            try {
                if (!nativeConnectControl(host, DEFAULT_CONTROL_PORT)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                _connectionState.value = ConnectionState.HANDSHAKING

                val hello = MicYouProtocol.encodeHello(deviceId, deviceName, sampleRate, channels)
                if (!nativeSendControlData(hello)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                val ackResult = nativeWaitForAck()
                if (!ackResult.success) {
                    if (ackResult.rejected) {
                        _connectionState.value = ConnectionState.REJECTED
                    } else {
                        _connectionState.value = ConnectionState.ERROR
                    }
                    return@launch
                }

                if (!nativeConnectAudio(host, DEFAULT_AUDIO_PORT)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                _connectionState.value = ConnectionState.CONNECTED

                nativeStartKeepAlive(deviceId)
                nativeStartAudioSender()

            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                disconnect()
            }
        }

        return true
    }

    fun sendAudio(pcmData: ByteArray, sampleRate: Int, channels: Int) {
        if (_connectionState.value != ConnectionState.CONNECTED &&
            _connectionState.value != ConnectionState.STREAMING) {
            return
        }

        val frame = IosAudioFrame(
            streamId = streamId,
            sequence = audioSequence++,
            timestamp = nativeGetTimestampMs(),
            sampleRate = sampleRate,
            channels = channels,
            pcm16le = pcmData
        )

        val packet = MicYouProtocol.encodeAudioFrame(frame)

        scope.launch {
            audioQueue.send(packet)
        }

        _connectionState.value = ConnectionState.STREAMING
    }

    fun disconnect() {
        scope.launch {
            try {
                if (controlConnected && streamId.isNotEmpty()) {
                    val disconnect = MicYouProtocol.encodeDisconnect(streamId)
                    nativeSendControlData(disconnect)
                }
            } catch (e: Exception) {
                // Ignore errors during disconnect
            }

            keepAliveJob?.cancel()
            audioQueue.close()

            nativeCloseSocket(controlSocket)
            nativeCloseSocket(audioSocket)

            controlSocket = -1
            audioSocket = -1
            controlConnected = false
            audioConnected = false

            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // Platform-specific socket operations - implemented in iosMain via extension functions
    internal fun nativeConnectControl(host: String, port: Int): Boolean = platformConnectControl(host, port)
    internal fun nativeConnectAudio(host: String, port: Int): Boolean = platformConnectAudio(host, port)
    internal fun nativeSendControlData(data: ByteArray): Boolean = platformSendControlData(data)
    internal fun nativeWaitForAck(): AckResult = platformWaitForAck()
    internal fun nativeStartKeepAlive(deviceId: String) = platformStartKeepAlive(deviceId)
    internal fun nativeStartAudioSender() = platformStartAudioSender()
    internal fun nativeCloseSocket(socket: Int) = platformCloseSocket(socket)
    internal fun nativeGetTimestampMs(): Long = platformGetTimestampMs()

    data class AckResult(val success: Boolean, val rejected: Boolean = false)
}

// Platform-specific implementations (defined in iosMain)
internal expect fun TransportClient.platformConnectControl(host: String, port: Int): Boolean
internal expect fun TransportClient.platformConnectAudio(host: String, port: Int): Boolean
internal expect fun TransportClient.platformSendControlData(data: ByteArray): Boolean
internal expect fun TransportClient.platformWaitForAck(): TransportClient.AckResult
internal expect fun TransportClient.platformStartKeepAlive(deviceId: String)
internal expect fun TransportClient.platformStartAudioSender()
internal expect fun TransportClient.platformCloseSocket(socket: Int)
internal expect fun TransportClient.platformGetTimestampMs(): Long
