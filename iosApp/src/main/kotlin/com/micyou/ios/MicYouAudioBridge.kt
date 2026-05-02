package com.micyou.ios

/**
 * Common interface for MicYouAudioBridge.
 * Platform-specific implementation is in iosMain.
 */
expect class MicYouAudioBridge() {
    fun prepareAudio(): String
    fun startCapture(): String
    fun stopCapture(): String
    fun setFrameCallback(callback: (pcmData: ByteArray, sampleRate: Int, channels: Int) -> Unit)
}
