package com.micyou.ios

class AudioFrameTransport {
    private val queue = ArrayDeque<ByteArray>()

    @Synchronized
    fun enqueue(frame: ByteArray) {
        queue.addLast(frame)
    }

    @Synchronized
    fun drain(): List<ByteArray> {
        val frames = queue.toList()
        queue.clear()
        return frames
    }
}
