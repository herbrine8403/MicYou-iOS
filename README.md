# MicYou iOS

MicYou iOS is a companion client scaffold for receiving MicYou-compatible audio streams on iPhone and iPad.

## Current approach
- Kotlin Multiplatform for shared protocol/data models
- Objective-C bridge layer for Apple platform interop
- Makefile-based entry points so the project can be driven without relying on an Xcode project file in the repository
- iOS 15+ target intent

## Status
This repository is currently a communication and app scaffold. The actual iOS audio session, capture, and playback implementation still needs native Apple APIs to be completed in an Apple build environment.
