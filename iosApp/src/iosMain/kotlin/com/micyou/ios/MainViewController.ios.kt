package com.micyou.ios

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS-specific implementation of MainViewController.
 */
actual fun MainViewController(): UIViewController = ComposeUIViewController {
    MicYouApp()
}
