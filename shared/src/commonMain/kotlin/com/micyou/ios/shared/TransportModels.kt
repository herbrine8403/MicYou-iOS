package com.micyou.ios.shared

import kotlinx.serialization.Serializable

@Serializable
data class MicYouPacket(
    val type: String,
    val sequence: Long,
    val payloadBase64: String,
)

@Serializable
data class MicYouHelloPacket(
    val deviceId: String,
    val deviceName: String,
    val sampleRate: Int,
    val channels: Int,
    val protocolVersion: Int = 1,
)
