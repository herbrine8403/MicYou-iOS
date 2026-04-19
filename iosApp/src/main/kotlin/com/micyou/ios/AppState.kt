package com.micyou.ios

data class AppState(
    val connected: Boolean = false,
    val status: String = "Idle",
    val host: String = "127.0.0.1",
    val port: String = "5000",
)
