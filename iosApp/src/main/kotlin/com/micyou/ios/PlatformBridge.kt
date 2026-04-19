package com.micyou.ios

expect class PlatformBridge {
    fun startConnection(host: String, port: Int)
    fun stopConnection()
}
