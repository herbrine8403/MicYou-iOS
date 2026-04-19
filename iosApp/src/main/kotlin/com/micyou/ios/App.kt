package com.micyou.ios

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun App() {
    val status = remember { mutableStateOf("Idle") }
    MaterialTheme {
        Button(onClick = { status.value = "Ready for connection" }) {
            Text("MicYou iOS scaffold: ${status.value}")
        }
    }
}
