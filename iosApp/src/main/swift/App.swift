import SwiftUI

/**
 * MicYou iOS App Entry Point.
 *
 * Supports iOS 14+ by using the standard SwiftUI App lifecycle.
 * The actual UI is rendered via Compose Multiplatform embedded in a SwiftUI view.
 */
@main
struct MicYouIOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
