package com.micyou.ios.shared

import kotlinx.serialization.Serializable

@Serializable
data class MicYouEndpoint(
    val host: String,
    val port: Int,
)

@Serializable
data class MicYouPairingRequest(
    val endpoint: MicYouEndpoint,
    val deviceId: String,
    val deviceName: String,
)

@Serializable
data class MicYouPairingResponse(
    val accepted: Boolean,
    val message: String,
)
