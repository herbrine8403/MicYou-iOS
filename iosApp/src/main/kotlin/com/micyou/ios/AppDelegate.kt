package com.micyou.ios

import UIKit.UIApplication
import UIKit.UIApplicationDelegate
import UIKit.UIApplicationLaunchOptionsKey
import platform.Foundation.NSDictionary

class AppDelegate : UIResponder(), UIApplicationDelegateProtocol {
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: NSDictionary<UIApplicationLaunchOptionsKey, *>?
    ): Boolean {
        return true
    }
}
