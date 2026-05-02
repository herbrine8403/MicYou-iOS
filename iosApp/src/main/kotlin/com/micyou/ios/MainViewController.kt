package com.micyou.ios

import androidx.compose.ui.window.ComposeUIViewController

/**
 * Main entry point for the iOS app.
 * Creates a Compose UIViewController that hosts the MicYou UI.
 */
fun MainViewController() = ComposeUIViewController {
    MicYouApp()
}
