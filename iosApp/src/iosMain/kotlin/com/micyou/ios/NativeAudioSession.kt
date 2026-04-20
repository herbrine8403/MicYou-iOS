package com.micyou.ios

import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioFrameCount
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFoundation.NSURL
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import kotlin.concurrent.Volatile

class NativeAudioSession {
    private val engine = AVAudioEngine()
    @Volatile private var isPrepared = false
    @Volatile private var isRunning = false
    @Volatile private var sampleRate: Double = 48000.0
    @Volatile private var channels: Int = 1
    private var onPcmFrame: ((ByteArray) -> Unit)? = null

    fun setFrameListener(listener: ((ByteArray) -> Unit)?) {
        onPcmFrame = listener
    }

    fun prepare(): String {
        return try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                category = AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionMixWithOthers,
                error = null
            )
            session.setPreferredSampleRate(sampleRate, error = null)
            session.setActive(true, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
            isPrepared = true
            "AVAudioSession prepared"
        } catch (t: Throwable) {
            NSLog("MicYou prepare failed: %@", t.message ?: "unknown")
            "Audio session prepare failed: ${t.message ?: "unknown"}"
        }
    }

    fun start(): String {
        if (!isPrepared) return "Audio session not prepared"
        return try {
            val input = engine.inputNode
            val inputFormat = input.inputFormatForBus(0u)
            val tapFormat = AVAudioFormat(standardFormatWithSampleRate = sampleRate, channels = channels.toULong())
                ?: inputFormat

            input.installTapOnBus(
                bus = 0u,
                bufferSize = 1024u,
                format = tapFormat,
            ) { buffer, _ ->
                val byteCount = (buffer.frameLength.toInt() * channels * 2)
                val bytes = ByteArray(byteCount)
                onPcmFrame?.invoke(bytes)
            }

            engine.prepare()
            engine.startAndReturnError(null)
            isRunning = true
            "AVAudioEngine capture started"
        } catch (t: Throwable) {
            NSLog("MicYou start failed: %@", t.message ?: "unknown")
            "Audio capture start failed: ${t.message ?: "unknown"}"
        }
    }

    fun stop(): String {
        return try {
            if (isRunning) {
                engine.inputNode.removeTapOnBus(0u)
                engine.stop()
                AVAudioSession.sharedInstance().setActive(false, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
            }
            isRunning = false
            "Audio capture stopped"
        } catch (t: Throwable) {
            "Audio capture stop failed: ${t.message ?: "unknown"}"
        }
    }

    fun isActive(): Boolean = isPrepared && isRunning
}
