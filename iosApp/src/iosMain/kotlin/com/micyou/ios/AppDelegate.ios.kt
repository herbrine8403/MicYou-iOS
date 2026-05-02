package com.micyou.ios

import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIResponder
import platform.Foundation.NSDictionary

/**
 * iOS-specific AppDelegate implementation.
 */
actual class AppDelegate actual constructor() : UIResponder(), UIApplicationDelegateProtocol {
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: NSDictionary?
    ): Boolean {
        return true
    }
}
