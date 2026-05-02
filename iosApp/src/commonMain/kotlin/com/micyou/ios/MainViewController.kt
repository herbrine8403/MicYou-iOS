package com.micyou.ios

/**
 * Common interface for MainViewController.
 * Platform-specific implementation is in iosMain.
 */
expect fun MainViewController(): platform.darwin.NSObject
