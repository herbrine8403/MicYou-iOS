@file:OptIn(ExperimentalForeignApi::class)

package com.micyou.ios

import kotlinx.cinterop.*
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.posix.*

// Platform-specific implementations for TransportClient

internal actual fun TransportClient.platformConnectControl(host: String, port: Int): Boolean {
    val fd = socket(AF_INET, SOCK_STREAM, 0)
    if (fd < 0) return false

    memScoped {
        val addr = alloc<sockaddr_in>()
        addr.sin_family = AF_INET.convert()
        addr.sin_port = htons(port.toUShort())
        inet_pton(AF_INET, host, addr.sin_addr.ptr)

        val result = connect(fd, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        if (result < 0) {
            close(fd)
            return false
        }
    }

    TransportClientNativeStore.controlSocket = fd
    return true
}

internal actual fun TransportClient.platformConnectAudio(host: String, port: Int): Boolean {
    val fd = socket(AF_INET, SOCK_DGRAM, 0)
    if (fd < 0) return false

    memScoped {
        val addr = alloc<sockaddr_in>()
        addr.sin_family = AF_INET.convert()
        addr.sin_port = htons(port.toUShort())
        inet_pton(AF_INET, host, addr.sin_addr.ptr)

        val result = connect(fd, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        if (result < 0) {
            close(fd)
            return false
        }
    }

    TransportClientNativeStore.audioSocket = fd
    return true
}

internal actual fun TransportClient.platformSendControlData(data: ByteArray): Boolean {
    val fd = TransportClientNativeStore.controlSocket
    if (fd < 0) return false

    val sent = data.usePinned { pinned ->
        send(fd, pinned.addressOf(0), data.size.convert(), 0)
    }
    return sent == data.size.convert()
}

internal actual fun TransportClient.platformWaitForAck(): TransportClient.AckResult {
    val fd = TransportClientNativeStore.controlSocket
    if (fd < 0) return TransportClient.AckResult(false)

    val buffer = ByteArray(1024)
    val received = buffer.usePinned { pinned ->
        recv(fd, pinned.addressOf(0), buffer.size.convert(), 0)
    }

    if (received <= 0) return TransportClient.AckResult(false)

    return try {
        val message = com.micyou.ios.shared.MicYouProtocol.decodeControlMessage(buffer.copyOf(received.toInt()))
        when (message.type) {
            com.micyou.ios.shared.IosMessageType.ACK -> TransportClient.AckResult(true)
            com.micyou.ios.shared.IosMessageType.ERROR -> TransportClient.AckResult(false, rejected = true)
            else -> TransportClient.AckResult(false)
        }
    } catch (e: Exception) {
        TransportClient.AckResult(false)
    }
}

internal actual fun TransportClient.platformStartKeepAlive(deviceId: String) {
    // Keepalive is managed by the common code using coroutines
}

internal actual fun TransportClient.platformStartAudioSender() {
    // Audio sender is managed by the common code using coroutines
}

internal actual fun TransportClient.platformCloseSocket(socket: Int) {
    if (socket >= 0) {
        close(socket)
    }
}

internal actual fun TransportClient.platformGetTimestampMs(): Long {
    return (NSDate().timeIntervalSince1970() * 1000).toLong()
}

/**
 * Store for native socket file descriptors.
 */
object TransportClientNativeStore {
    var controlSocket: Int = -1
    var audioSocket: Int = -1
}
