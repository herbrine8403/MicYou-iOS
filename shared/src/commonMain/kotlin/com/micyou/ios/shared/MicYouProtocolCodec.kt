package com.micyou.ios.shared

import kotlinx.serialization.json.Json

object MicYouProtocolCodec {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun encodeHandshake(message: MicYouHandshake): String = json.encodeToString(MicYouHandshake.serializer(), message)
    fun encodePairingRequest(message: MicYouPairingRequest): String = json.encodeToString(MicYouPairingRequest.serializer(), message)
    fun encodePairingResponse(message: MicYouPairingResponse): String = json.encodeToString(MicYouPairingResponse.serializer(), message)
    fun encodeSessionState(message: MicYouSessionState): String = json.encodeToString(MicYouSessionState.serializer(), message)
}
