package com.micyou.ios

import com.micyou.ios.shared.MicYouPacket
import kotlin.random.Random

class TransportClient(
    private val host: String,
    private val port: Int,
) {
    private var connected = false
    private var sequence = 0L

    fun connect(): String {
        connected = true
        return "Connected to $host:$port"
    }

    fun disconnect(): String {
        connected = false
        return "Disconnected"
    }

    fun sendHello(): String {
        if (!connected) return "Transport not connected"
        val packet = MicYouPacket(
            type = "hello",
            sequence = ++sequence,
            payloadBase64 = Random.nextBytes(8).joinToString(separator = "") { it.toUByte().toString(16) }
        )
        return "Sent hello packet ${packet.sequence}"
    }

    fun sendAudio(frame: ByteArray): String {
        if (!connected) return "Transport not connected"
        val packet = MicYouPacket(
            type = "audio",
            sequence = ++sequence,
            payloadBase64 = frame.joinToString(separator = "") { it.toUByte().toString(16) }
        )
        return "Sent audio packet ${packet.sequence}"
    }
}
