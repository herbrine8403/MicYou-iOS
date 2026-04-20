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
    val vm = remember { ConnectionViewModel() }

    MaterialTheme {
        Column {
            Text("MicYou iOS")
            Text("Host: ${host.value}")
            Text("Port: ${port.value}")
            Button(onClick = {
                vm.updateHost(host.value)
                vm.updatePort(port.value)
                vm.prepareAudio()
                status.value = vm.currentState().status
            }) {
                Text("Prepare Audio Capture")
            }
            Button(onClick = {
                vm.updateHost(host.value)
                vm.updatePort(port.value)
                vm.connect()
                status.value = vm.currentState().status
            }) {
                Text("Connect")
            }
            Button(onClick = {
                vm.startCapture()
                status.value = vm.currentState().status
            }) {
                Text("Start Capture")
            }
            Button(onClick = {
                vm.stopCapture()
                vm.disconnect()
                status.value = vm.currentState().status
            }) {
                Text("Disconnect")
            }
            Text("Status: ${status.value}")
        }
    }
}
