package com.micyou.ios

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun createMainViewController(): UIViewController = ComposeUIViewController {
    MicYouApp()
}
