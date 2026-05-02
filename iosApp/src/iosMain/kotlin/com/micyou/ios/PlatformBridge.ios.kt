package com.micyou.ios

/**
 * iOS-specific platform bridge for audio capture.
 */
actual class PlatformBridge {
    private val bridge = MicYouAudioBridge()

    actual fun startConnection(host: String, port: Int) {
        bridge.start(host, port.toLong())
    }

    actual fun stopConnection() {
        bridge.stop()
    }
}
