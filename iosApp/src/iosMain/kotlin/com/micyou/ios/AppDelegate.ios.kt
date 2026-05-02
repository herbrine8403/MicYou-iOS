package com.micyou.ios

import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIResponder

/**
 * iOS-specific AppDelegate implementation.
 */
actual class AppDelegate actual constructor() : UIResponder(), UIApplicationDelegateProtocol {
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?
    ): Boolean {
        return true
    }
}
