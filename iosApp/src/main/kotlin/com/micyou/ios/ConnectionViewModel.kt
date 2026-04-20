package com.micyou.ios

class ConnectionViewModel {
    private val bridge = PlatformBridge()
    private val audioSession = AudioSessionManager()
    private var state = AppState()

    fun updateHost(host: String) {
        state = state.copy(host = host)
    }

    fun updatePort(port: String) {
        state = state.copy(port = port)
    }

    fun prepareAudio() {
        state = state.copy(status = audioSession.prepare())
    }

    fun connect() {
        val portNumber = state.port.toIntOrNull() ?: 5000
        state = state.copy(status = audioSession.attachTransport(state.host, portNumber))
        state = state.copy(connected = true, status = audioSession.sendHello())
        bridge.startConnection(state.host, portNumber)
    }

    fun startCapture() {
        state = state.copy(status = audioSession.startCapture())
    }

    fun stopCapture() {
        state = state.copy(status = audioSession.stopCapture())
    }

    fun disconnect() {
        bridge.stopConnection()
        state = state.copy(connected = false, status = "Disconnected")
    }

    fun currentState(): AppState = state
}
