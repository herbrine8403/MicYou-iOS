@file:OptIn(ExperimentalForeignApi::class)

package com.micyou.ios

import kotlinx.cinterop.*
import platform.AVFAudio.*
import platform.CoreAudioTypes.*
import platform.Foundation.NSData
import platform.Foundation.dataWithBytesNoCopy
import platform.posix.memcpy

/**
 * iOS-specific implementation of MicYouAudioBridge using AVAudioEngine directly.
 */
actual class MicYouAudioBridge actual constructor() {
    private var audioEngine: AVAudioEngine? = null
    private var audioSession: AVAudioSession? = null
    private var frameCallback: ((ByteArray, Int, Int) -> Unit)? = null
    private var sampleRate = 48000
    private var channels = 1
    private var isCapturing = false

    actual fun prepareAudio(): String {
        val session = AVAudioSession.sharedInstance()
        audioSession = session

        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val success = session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                AVAudioSessionModeDefault,
                AVAudioSessionCategoryOptionDefaultToSpeaker or
                        AVAudioSessionCategoryOptionAllowBluetooth or
                        AVAudioSessionCategoryOptionAllowBluetoothA2DP,
                errorPtr.ptr
            )
            if (!success) {
                return "Failed to set category: ${errorPtr.value?.localizedDescription ?: "unknown"}"
            }

            val activeSuccess = session.setActive(true, errorPtr.ptr)
            if (!activeSuccess) {
                return "Failed to activate session: ${errorPtr.value?.localizedDescription ?: "unknown"}"
            }
        }

        sampleRate = session.sampleRate().toInt()
        if (sampleRate <= 0) sampleRate = 48000

        val engine = AVAudioEngine()
        audioEngine = engine

        val inputNode = engine.inputNode()
        val inputFormat = inputNode.outputFormatForBus(0u)
        channels = inputFormat.channelCount.toInt()
        if (channels <= 0) channels = 1

        val recordingFormat = AVAudioFormat(
            commonFormat = AVAudioPCMFormatInt16,
            sampleRate = sampleRate.toDouble(),
            channels = channels.toUInt(),
            interleaved = true
        )

        if (recordingFormat == null) {
            return "Failed to create recording format"
        }

        inputNode.installTapOnBus(
            0u,
            480u,
            recordingFormat
        ) { buffer, _ ->
            processAudioBuffer(buffer)
        }

        return "Audio prepared: ${sampleRate}Hz, ${channels}ch"
    }

    actual fun startCapture(): String {
        if (isCapturing) return "Already capturing"

        val engine = audioEngine ?: return "Audio not prepared"

        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val success = engine.startAndReturnError(errorPtr.ptr)
            if (!success) {
                return "Failed to start engine: ${errorPtr.value?.localizedDescription ?: "unknown"}"
            }
        }

        isCapturing = true
        return "Capture started: ${sampleRate}Hz, ${channels}ch"
    }

    actual fun stopCapture(): String {
        if (!isCapturing) return "Not capturing"

        audioEngine?.inputNode()?.removeTapOnBus(0u)
        audioEngine?.stop()

        val session = audioSession
        if (session != null) {
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                session.setActive(false, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, errorPtr.ptr)
            }
        }

        isCapturing = false
        return "Capture stopped"
    }

    actual fun setFrameCallback(callback: (pcmData: ByteArray, sampleRate: Int, channels: Int) -> Unit) {
        frameCallback = callback
    }

    private fun processAudioBuffer(buffer: AVAudioPCMBuffer?) {
        if (buffer == null) return
        val callback = frameCallback ?: return

        val frameLength = buffer.frameLength.toInt()
        if (frameLength == 0) return

        val int16ChannelData = buffer.int16ChannelData
        if (int16ChannelData == null) return

        val int16Data = int16ChannelData.get(0)
        if (int16Data == null) return

        val byteLength = frameLength * channels * 2
        val pcmData = ByteArray(byteLength)

        pcmData.usePinned { pinned ->
            memcpy(pinned.addressOf(0), int16Data, byteLength.toULong())
        }

        callback(pcmData, sampleRate, channels)
    }
}
