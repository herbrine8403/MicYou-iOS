package com.micyou.ios

class AudioSessionManager {
    fun prepare(sampleRate: Int = 48000, channels: Int = 2): String {
        return "Audio session prepared: ${sampleRate}Hz, ${channels}ch"
    }

    fun startCapture(): String {
        return "Audio capture started"
    }

    fun stopCapture(): String {
        return "Audio capture stopped"
    }
}
