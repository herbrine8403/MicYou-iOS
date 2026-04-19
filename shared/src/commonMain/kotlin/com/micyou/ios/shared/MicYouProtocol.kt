package com.micyou.ios.shared

import kotlinx.serialization.Serializable

@Serializable
data class MicYouHandshake(
    val deviceId: String,
    val deviceName: String,
    val sampleRate: Int,
    val channels: Int,
    val protocolVersion: Int = 1,
)

@Serializable
data class MicYouAudioFrame(
    val streamId: String,
    val sequence: Long,
    val timestampMillis: Long,
    val pcm16leBase64: String,
)

@Serializable
data class MicYouControlMessage(
    val type: String,
    val payload: String = "",
)

@Serializable
data class MicYouSessionState(
    val connected: Boolean,
    val status: String,
)
