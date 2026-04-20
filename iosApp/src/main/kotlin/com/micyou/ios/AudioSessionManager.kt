package com.micyou.ios

class AudioSessionManager {
    private val native = NativeAudioSession()
    private val frames = AudioFrameTransport()
    private var transport: TransportClient? = null

    init {
        native.setFrameListener { pcm ->
            frames.enqueue(pcm)
            transport?.sendAudio(pcm)
        }
    }

    fun prepare(sampleRate: Int = 48000, channels: Int = 1): String {
        return native.prepare()
    }

    fun attachTransport(host: String, port: Int): String {
        transport = TransportClient(host, port)
        return transport?.connect() ?: "Transport attach failed"
    }

    fun sendHello(): String {
        return transport?.sendHello() ?: "Transport not attached"
    }

    fun startCapture(): String {
        return native.start()
    }

    fun stopCapture(): String {
        val stopped = native.stop()
        transport?.disconnect()
        return stopped
    }

    fun drainFrames(): List<ByteArray> = frames.drain()
}
