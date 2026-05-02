# MicYou iOS

MicYou iOS is a companion client for receiving MicYou-compatible audio streams on iPhone and iPad.

## Architecture

- **Kotlin Multiplatform** for shared protocol/data models
- **Objective-C bridge** (`MicYouAudioBridge`) for Apple platform audio capture via `AVAudioEngine`
- **SwiftUI** hosts the Compose Multiplatform UI via `UIViewControllerRepresentable`
- **Makefile**-based entry points so the project can be driven without relying on an Xcode project file in the repository

## Requirements

- iOS 14+
- Xcode 15+
- macOS 14+ (for building)

## Project Structure

```
MicYou-iOS/
├── iosApp/                    # iOS application module
│   ├── src/
│   │   ├── main/kotlin/       # Kotlin common + iOS-specific code
│   │   │   ├── MicYouApp.kt   # Main Compose UI
│   │   │   ├── AudioSessionManager.kt  # Audio session orchestration
│   │   │   ├── TransportClient.kt      # Network transport (TCP+UDP)
│   │   │   └── MainViewController.kt   # iOS entry point
│   │   └── main/
│   │       ├── objc/          # Objective-C bridge headers
│   │       └── swift/         # SwiftUI app entry
│   └── build.gradle.kts
├── shared/                    # KMP shared module
│   └── src/commonMain/kotlin/
│       └── MicYouProtocol.kt  # iOS <-> PC protocol definitions
├── Makefile                   # Build automation
└── .github/workflows/         # CI/CD
```

## Building

```bash
# Using Gradle (KMP build)
./gradlew build

# Using Makefile (Xcode build)
make build              # Simulator
make build-device       # Physical device
make archive            # Create .xcarchive
make ipa                # Export .ipa
```

## Communication Protocol

The iOS client communicates with the PC via the `ios2pc-myp` plugin:

1. **TCP Control Channel**: Handshake (HELLO/ACK), keepalive, disconnect
2. **UDP Audio Channel**: Real-time PCM16LE audio streaming

Message format:
- 4 bytes: Magic (`0x694F5354` = "iOST")
- 4 bytes: Payload length (big-endian)
- N bytes: Protobuf-encoded payload

## Usage

1. Install and enable the `ios2pc-myp` plugin on the MicYou desktop app
2. Note the plugin's control port and audio port
3. Enter the PC's IP address and ports in the iOS app
4. Tap "Prepare" -> "Connect" -> "Start Capture"

## License

MIT
