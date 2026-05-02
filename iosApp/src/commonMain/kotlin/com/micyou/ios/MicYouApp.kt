package com.micyou.ios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Main MicYou iOS application UI.
 *
 * Uses fixed ports (16000/16001) matching the ios2pc-myp plugin.
 * Simplified UI - only requires PC IP address.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicYouApp() {
    val scope = rememberCoroutineScope()
    val audioManager = remember { AudioSessionManager() }

    var host by remember { mutableStateOf("192.168.1.100") }
    var statusMessage by remember { mutableStateOf("Ready") }
    var isConnecting by remember { mutableStateOf(false) }

    val audioStatus by audioManager.audioStatus.collectAsState()
    val connectionState by remember {
        derivedStateOf { audioManager.getConnectionState() }
    }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MicYou iOS") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConnectionStateCard(
                    state = connectionState,
                    audioStatus = audioStatus
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("PC IP Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = audioManager.prepare()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !audioStatus.isCapturing
                    ) {
                        Text("Prepare")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isConnecting = true
                                statusMessage = audioManager.attachTransport(host)
                                isConnecting = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !audioStatus.isConnected && !isConnecting
                    ) {
                        Text(if (isConnecting) "Connecting..." else "Connect")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = audioManager.startCapture()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = audioStatus.isConnected && !audioStatus.isCapturing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Start Capture")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = audioManager.stopCapture()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = audioStatus.isCapturing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop")
                    }
                }

                OutlinedButton(
                    onClick = {
                        audioManager.disconnect()
                        statusMessage = "Disconnected"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = audioStatus.isConnected
                ) {
                    Text("Disconnect")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Status: $statusMessage",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (audioStatus.isCapturing) {
                            Text(
                                text = "Audio: ${audioStatus.sampleRate}Hz, ${audioStatus.channels}ch",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Sent: ${audioStatus.packetsSent} packets, ${formatBytes(audioStatus.bytesSent)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Text(
                    text = "Ports: Control ${TransportClient.DEFAULT_CONTROL_PORT} (TCP), Audio ${TransportClient.DEFAULT_AUDIO_PORT} (UDP)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConnectionStateCard(
    state: TransportClient.ConnectionState,
    audioStatus: com.micyou.ios.shared.AudioStatus
) {
    val (color, text) = when (state) {
        TransportClient.ConnectionState.DISCONNECTED ->
            MaterialTheme.colorScheme.error to "Disconnected"
        TransportClient.ConnectionState.CONNECTING ->
            MaterialTheme.colorScheme.tertiary to "Connecting..."
        TransportClient.ConnectionState.HANDSHAKING ->
            MaterialTheme.colorScheme.tertiary to "Handshaking..."
        TransportClient.ConnectionState.CONNECTED ->
            MaterialTheme.colorScheme.primary to "Connected"
        TransportClient.ConnectionState.STREAMING ->
            MaterialTheme.colorScheme.primary to "Streaming Audio"
        TransportClient.ConnectionState.ERROR ->
            MaterialTheme.colorScheme.error to "Connection Error"
        TransportClient.ConnectionState.REJECTED ->
            MaterialTheme.colorScheme.error to "Server Busy (Another device connected)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )

            if (audioStatus.isCapturing) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.primary)
                    }
                ) {
                    Text("REC", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> {
            val mb = bytes / (1024.0 * 1024.0)
            val rounded = (mb * 100).toInt() / 100.0
            "$rounded MB"
        }
        bytes >= 1024 -> {
            val kb = bytes / 1024.0
            val rounded = (kb * 100).toInt() / 100.0
            "$rounded KB"
        }
        else -> "$bytes B"
    }
}
