package com.micyou.ios.shared

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Shared protocol definitions for MicYou iOS <-> PC communication.
 *
 * This protocol is designed to be compatible with the ios2pc-myp plugin
 * running on the MicYou desktop host.
 *
 * Message format:
 * - 4 bytes: Magic (0x694F5354 = "iOST")
 * - 4 bytes: Payload length (big-endian)
 * - N bytes: Protobuf-encoded payload
 */

const val IOS_MAGIC = 0x694F5354 // "iOST"

@OptIn(ExperimentalSerializationApi::class)
object MicYouProtocol {
    private val proto = ProtoBuf { }

    fun encodeControlMessage(message: IosControlMessage): ByteArray {
        val payload = proto.encodeToByteArray(IosControlMessage.serializer(), message)
        return wrapWithHeader(payload)
    }

    fun decodeControlMessage(data: ByteArray): IosControlMessage {
        val payload = unwrapHeader(data)
        return proto.decodeFromByteArray(IosControlMessage.serializer(), payload)
    }

    fun encodeAudioFrame(frame: IosAudioFrame): ByteArray {
        val payload = proto.encodeToByteArray(IosAudioFrame.serializer(), frame)
        return wrapWithHeader(payload)
    }

    fun decodeAudioFrame(data: ByteArray): IosAudioFrame {
        val payload = unwrapHeader(data)
        return proto.decodeFromByteArray(IosAudioFrame.serializer(), payload)
    }

    fun encodeHello(deviceId: String, deviceName: String, sampleRate: Int, channels: Int): ByteArray {
        return encodeControlMessage(
            IosControlMessage(
                type = IosMessageType.HELLO,
                deviceId = deviceId,
                deviceName = deviceName,
                sampleRate = sampleRate,
                channels = channels
            )
        )
    }

    fun encodeKeepAlive(deviceId: String, sequence: Int = 0): ByteArray {
        return encodeControlMessage(
            IosControlMessage(
                type = IosMessageType.KEEPALIVE,
                deviceId = deviceId,
                sequence = sequence
            )
        )
    }

    fun encodeDisconnect(deviceId: String, reason: String = ""): ByteArray {
        return encodeControlMessage(
            IosControlMessage(
                type = IosMessageType.DISCONNECT,
                deviceId = deviceId,
                payload = reason
            )
        )
    }

    /**
     * Write Int as big-endian bytes.
     */
    private fun Int.toBigEndianBytes(): ByteArray = byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )

    /**
     * Read big-endian Int from byte array at offset.
     */
    private fun ByteArray.readBigEndianInt(offset: Int): Int {
        return ((this[offset].toInt() and 0xFF) shl 24) or
                ((this[offset + 1].toInt() and 0xFF) shl 16) or
                ((this[offset + 2].toInt() and 0xFF) shl 8) or
                (this[offset + 3].toInt() and 0xFF)
    }

    private fun wrapWithHeader(payload: ByteArray): ByteArray {
        val result = ByteArray(8 + payload.size)
        // Write magic (big-endian)
        val magicBytes = IOS_MAGIC.toBigEndianBytes()
        magicBytes.copyInto(result, 0)
        // Write length (big-endian)
        val lengthBytes = payload.size.toBigEndianBytes()
        lengthBytes.copyInto(result, 4)
        // Write payload
        payload.copyInto(result, 8)
        return result
    }

    private fun unwrapHeader(data: ByteArray): ByteArray {
        if (data.size < 8) {
            throw IllegalArgumentException("Data too small for header: ${data.size} bytes")
        }
        val magic = data.readBigEndianInt(0)
        if (magic != IOS_MAGIC) {
            throw IllegalArgumentException("Magic mismatch: expected 0x${IOS_MAGIC.toString(16)}, got 0x${magic.toString(16)}")
        }
        val length = data.readBigEndianInt(4)
        if (length < 0 || length > data.size - 8) {
            throw IllegalArgumentException("Invalid payload length: $length")
        }
        return data.copyOfRange(8, 8 + length)
    }
}

@Serializable
enum class IosMessageType {
    HELLO,
    ACK,
    KEEPALIVE,
    DISCONNECT,
    CONFIG,
    ERROR
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class IosControlMessage(
    val type: IosMessageType,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    val sequence: Int = 0,
    val controlPort: Int = 0,
    val audioPort: Int = 0,
    val payload: String = ""
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class IosAudioFrame(
    val streamId: String,
    val sequence: Long,
    val timestamp: Long,
    val sampleRate: Int,
    val channels: Int,
    val pcm16le: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as IosAudioFrame
        return streamId == other.streamId &&
                sequence == other.sequence &&
                timestamp == other.timestamp &&
                sampleRate == other.sampleRate &&
                channels == other.channels &&
                pcm16le.contentEquals(other.pcm16le)
    }

    override fun hashCode(): Int {
        var result = streamId.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + pcm16le.contentHashCode()
        return result
    }
}

@Serializable
data class ConnectionConfig(
    val host: String,
    val controlPort: Int,
    val audioPort: Int,
    val deviceId: String,
    val deviceName: String
)
