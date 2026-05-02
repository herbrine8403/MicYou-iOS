package com.micyou.ios

/**
 * Common interface for platform-specific audio bridge.
 */
expect class PlatformBridge() {
    fun startConnection(host: String, port: Int)
    fun stopConnection()
}
