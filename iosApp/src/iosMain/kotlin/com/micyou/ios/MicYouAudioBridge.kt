@file:OptIn(ExperimentalForeignApi::class)

package com.micyou.ios

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.posix.memcpy
import objc.MicYouAudioBridgeOC

/**
 * iOS-specific implementation of MicYouAudioBridge.
 *
 * Wraps the Objective-C MicYouAudioBridgeOC to provide audio capture.
 */
actual class MicYouAudioBridge actual constructor() {
    private val objcBridge = MicYouAudioBridgeHelper()

    actual fun prepareAudio(): String {
        return objcBridge.prepareAudio()
    }

    actual fun startCapture(): String {
        return objcBridge.startCapture()
    }

    actual fun stopCapture(): String {
        return objcBridge.stopCapture()
    }

    actual fun setFrameCallback(callback: (pcmData: ByteArray, sampleRate: Int, channels: Int) -> Unit) {
        objcBridge.setFrameCallback { nsData, sampleRate, channels ->
            val pcmData = nsData.toByteArray()
            callback(pcmData, sampleRate, channels)
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        val dataLength = this.length.toInt()
        return ByteArray(dataLength).apply {
            if (dataLength > 0) {
                this.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), this@toByteArray.bytes, dataLength.toULong())
                }
            }
        }
    }
}

/**
 * Helper class to bridge Objective-C MicYouAudioBridgeOC to Kotlin.
 */
private class MicYouAudioBridgeHelper {
    private val bridge = MicYouAudioBridgeOC()

    fun prepareAudio(): String {
        return bridge.prepareAudio()
    }

    fun startCapture(): String {
        return bridge.startCapture()
    }

    fun stopCapture(): String {
        return bridge.stopCapture()
    }

    fun setFrameCallback(callback: (pcmData: ByteArray, sampleRate: Int, channels: Int) -> Unit) {
        bridge.setFrameCallback { nsData: NSData, sampleRate: Int, channels: Int ->
            val pcmData = ByteArray(nsData.length.toInt())
            if (pcmData.isNotEmpty()) {
                pcmData.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length.toULong())
                }
            }
            callback(pcmData, sampleRate, channels)
        }
    }
}
