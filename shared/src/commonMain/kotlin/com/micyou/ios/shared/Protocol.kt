package com.micyou.ios.shared

import kotlinx.serialization.Serializable

@Serializable
data class MicYouFrame(
    val streamId: String,
    val sampleRate: Int,
    val channels: Int,
    val pcm16le: ByteArray,
)

@Serializable
data class MicYouConnectionConfig(
    val host: String,
    val port: Int,
    val authToken: String? = null,
)
