package com.micyou.ios.shared

import kotlinx.serialization.Serializable

@Serializable
data class MicYouAudioConfig(
    val sampleRate: Int = 48000,
    val channels: Int = 2,
    val bitDepth: Int = 16,
)

@Serializable
data class MicYouAudioChunk(
    val streamId: String,
    val sequence: Long,
    val timestampMillis: Long,
    val pcm16leBase64: String,
)
