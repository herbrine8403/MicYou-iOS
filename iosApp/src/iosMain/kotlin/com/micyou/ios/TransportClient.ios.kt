package com.micyou.ios

import com.micyou.ios.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import platform.Foundation.*
import platform.posix.*

/**
 * iOS-specific implementation of TransportClient using POSIX sockets.
 */
actual class TransportClient actual constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var controlSocket: Int = -1
    private var controlConnected = false

    private var audioSocket: Int = -1
    private var audioConnected = false

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    actual val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(NetworkStats())
    actual val stats: StateFlow<NetworkStats> = _stats.asStateFlow()

    private var keepAliveJob: Job? = null
    private var audioSequence = 0L
    private var streamId = ""

    private val audioQueue = Channel<ByteArray>(Channel.BUFFERED)

    actual companion object {
        actual val DEFAULT_CONTROL_PORT = 16000
        actual val DEFAULT_AUDIO_PORT = 16001
    }

    actual enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        HANDSHAKING,
        CONNECTED,
        STREAMING,
        ERROR,
        REJECTED
    }

    actual fun connect(
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
                if (!connectControl(host, DEFAULT_CONTROL_PORT)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                _connectionState.value = ConnectionState.HANDSHAKING

                val hello = MicYouProtocol.encodeHello(deviceId, deviceName, sampleRate, channels)
                if (!sendControlData(hello)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                val ackResult = waitForAck()
                if (!ackResult.success) {
                    if (ackResult.rejected) {
                        _connectionState.value = ConnectionState.REJECTED
                    } else {
                        _connectionState.value = ConnectionState.ERROR
                    }
                    return@launch
                }

                if (!connectAudio(host, DEFAULT_AUDIO_PORT)) {
                    _connectionState.value = ConnectionState.ERROR
                    return@launch
                }

                _connectionState.value = ConnectionState.CONNECTED

                startKeepAlive(deviceId)
                startAudioSender()

            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                disconnect()
            }
        }

        return true
    }

    actual fun sendAudio(pcmData: ByteArray, sampleRate: Int, channels: Int) {
        if (_connectionState.value != ConnectionState.CONNECTED &&
            _connectionState.value != ConnectionState.STREAMING) {
            return
        }

        val frame = IosAudioFrame(
            streamId = streamId,
            sequence = audioSequence++,
            timestamp = getTimestampMs(),
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

    actual fun disconnect() {
        scope.launch {
            try {
                if (controlConnected && streamId.isNotEmpty()) {
                    val disconnect = MicYouProtocol.encodeDisconnect(streamId)
                    sendControlData(disconnect)
                }
            } catch (e: Exception) {
                // Ignore errors during disconnect
            }

            keepAliveJob?.cancel()
            audioQueue.close()

            closeSocket(controlSocket)
            closeSocket(audioSocket)

            controlSocket = -1
            audioSocket = -1
            controlConnected = false
            audioConnected = false

            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // --- Private ---

    private fun connectControl(host: String, port: Int): Boolean {
        controlSocket = socket(AF_INET, SOCK_STREAM, 0)
        if (controlSocket < 0) return false

        memScoped {
            val serverAddr = alloc<sockaddr_in>()
            serverAddr.sin_family = AF_INET.convert()
            serverAddr.sin_port = htons(port.convert())
            inet_pton(AF_INET, host, &serverAddr.sin_addr)

            val result = connect(
                controlSocket,
                serverAddr.ptr.reinterpret(),
                sizeOf<sockaddr_in>().convert()
            )

            if (result < 0) {
                closeSocket(controlSocket)
                controlSocket = -1
                return false
            }
        }

        controlConnected = true
        return true
    }

    private fun connectAudio(host: String, port: Int): Boolean {
        audioSocket = socket(AF_INET, SOCK_DGRAM, 0)
        if (audioSocket < 0) return false

        memScoped {
            val serverAddr = alloc<sockaddr_in>()
            serverAddr.sin_family = AF_INET.convert()
            serverAddr.sin_port = htons(port.convert())
            inet_pton(AF_INET, host, &serverAddr.sin_addr)

            val result = connect(
                audioSocket,
                serverAddr.ptr.reinterpret(),
                sizeOf<sockaddr_in>().convert()
            )

            if (result < 0) {
                closeSocket(audioSocket)
                audioSocket = -1
                return false
            }
        }

        audioConnected = true
        return true
    }

    private fun sendControlData(data: ByteArray): Boolean {
        if (!controlConnected || controlSocket < 0) return false

        val sent = data.usePinned { pinned ->
            send(controlSocket, pinned.addressOf(0), data.size.convert(), 0)
        }

        return sent == data.size.convert()
    }

    private data class AckResult(val success: Boolean, val rejected: Boolean = false)

    private fun waitForAck(): AckResult {
        if (!controlConnected) return AckResult(false)

        val buffer = ByteArray(1024)
        val received = buffer.usePinned { pinned ->
            recv(controlSocket, pinned.addressOf(0), buffer.size.convert(), 0)
        }

        if (received <= 0) return AckResult(false)

        return try {
            val message = MicYouProtocol.decodeControlMessage(buffer.copyOf(received.toInt()))
            when (message.type) {
                IosMessageType.ACK -> AckResult(true)
                IosMessageType.ERROR -> AckResult(false, rejected = true)
                else -> AckResult(false)
            }
        } catch (e: Exception) {
            AckResult(false)
        }
    }

    private fun startKeepAlive(deviceId: String) {
        keepAliveJob = scope.launch {
            var sequence = 0
            while (isActive && controlConnected) {
                delay(5000)
                try {
                    val keepAlive = MicYouProtocol.encodeKeepAlive(deviceId, sequence++)
                    sendControlData(keepAlive)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun startAudioSender() {
        scope.launch {
            for (packet in audioQueue) {
                if (!audioConnected || audioSocket < 0) continue

                packet.usePinned { pinned ->
                    send(audioSocket, pinned.addressOf(0), packet.size.convert(), 0)
                }
            }
        }
    }

    private fun closeSocket(socket: Int) {
        if (socket >= 0) {
            close(socket)
        }
    }

    private fun getTimestampMs(): Long {
        return NSDate().timeIntervalSince1970.toLong() * 1000
    }
}
