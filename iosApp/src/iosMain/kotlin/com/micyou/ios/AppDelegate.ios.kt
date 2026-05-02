package com.micyou.ios

import UIKit.UIApplication
import UIKit.UIApplicationDelegateProtocol
import UIKit.UIResponder
import platform.Foundation.NSDictionary

/**
 * iOS-specific AppDelegate implementation.
 */
actual class AppDelegate actual constructor() : UIResponder(), UIApplicationDelegateProtocol {
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: NSDictionary<*, *>?
    ): Boolean {
        return true
    }
}
