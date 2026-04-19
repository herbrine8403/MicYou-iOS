package com.micyou.ios

import androidx.compose.material3.Button
import androidx.compose.material3.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun MicYouApp() {
    val status = remember { mutableStateOf("Idle") }
    val host = remember { mutableStateOf("127.0.0.1") }
    val port = remember { mutableStateOf("5000") }

    MaterialTheme {
        Column {
            Text("MicYou iOS")
            Text("Host: ${host.value}")
            Text("Port: ${port.value}")
            Button(onClick = { status.value = "Audio capture scaffold ready" }) {
                Text("Prepare Audio Capture")
            }
            Button(onClick = { status.value = "Connected to host" }) {
                Text("Connect")
            }
            Button(onClick = { status.value = "Disconnected" }) {
                Text("Disconnect")
            }
            Text("Status: ${status.value}")
        }
    }
}
