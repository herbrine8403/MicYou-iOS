import SwiftUI

/**
 * SwiftUI ContentView that embeds the Compose Multiplatform UI.
 *
 * This serves as the bridge between SwiftUI and the Kotlin/Compose
 * based MicYouApp.
 */
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

/**
 * UIViewControllerRepresentable wrapper for the Compose UIViewController.
 */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return IosAppEntryKt.createMainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
