# MicYou iOS Makefile
# Provides build targets that can be driven without Xcode project files in the repo.

.PHONY: all build test clean ipa archive

# Project configuration
PROJECT_NAME := MicYou-iOS
SCHEME := MicYouIOS
BUNDLE_ID := com.micyou.ios

# Build directories
BUILD_DIR := build
DERIVED_DATA := $(BUILD_DIR)/DerivedData
ARCHIVE_PATH := $(BUILD_DIR)/MicYou-iOS.xcarchive
IPA_PATH := $(BUILD_DIR)/MicYou-iOS.ipa

# Default target
all: build

# Build the project using xcodebuild
# Requires: Xcode, macOS, iOS SDK
build:
	@echo "Building $(PROJECT_NAME)..."
	mkdir -p $(BUILD_DIR)
	xcodebuild \
		-project iosApp.xcodeproj \
		-scheme $(SCHEME) \
		-destination 'platform=iOS Simulator,name=iPhone 15' \
		-derivedDataPath $(DERIVED_DATA) \
		build

# Build for device (requires signing)
build-device:
	@echo "Building $(PROJECT_NAME) for device..."
	mkdir -p $(BUILD_DIR)
	xcodebuild \
		-project iosApp.xcodeproj \
		-scheme $(SCHEME) \
		-destination 'generic/platform=iOS' \
		-derivedDataPath $(DERIVED_DATA) \
		build

# Create archive for distribution
archive:
	@echo "Creating archive..."
	mkdir -p $(BUILD_DIR)
	xcodebuild \
		-project iosApp.xcodeproj \
		-scheme $(SCHEME) \
		-destination 'generic/platform=iOS' \
		-archivePath $(ARCHIVE_PATH) \
		archive

# Export IPA from archive
# Requires: export-options.plist
ipa: archive
	@echo "Exporting IPA..."
	xcodebuild \
		-exportArchive \
		-archivePath $(ARCHIVE_PATH) \
		-exportPath $(BUILD_DIR) \
		-exportOptionsPlist scripts/export-options.plist

# Run tests
test:
	@echo "Running tests..."
	xcodebuild \
		-project iosApp.xcodeproj \
		-scheme $(SCHEME) \
		-destination 'platform=iOS Simulator,name=iPhone 15' \
		test

# Clean build artifacts
clean:
	@echo "Cleaning..."
	rm -rf $(BUILD_DIR)
	rm -rf iosApp/build
	rm -rf shared/build

# Gradle wrapper tasks (for KMP build)
gradle-build:
	./gradlew build

gradle-test:
	./gradlew test

# Setup - install dependencies
setup:
	@echo "Setting up $(PROJECT_NAME)..."
	@echo "Ensure you have:"
	@echo "  - Xcode 15+"
	@echo "  - macOS 14+"
	@echo "  - iOS 14+ SDK"
	@echo "  - Kotlin Multiplatform Mobile plugin (optional)"

# Help
help:
	@echo "MicYou iOS Makefile"
	@echo ""
	@echo "Targets:"
	@echo "  build         - Build for iOS Simulator"
	@echo "  build-device  - Build for physical device"
	@echo "  archive       - Create .xcarchive for distribution"
	@echo "  ipa           - Export .ipa from archive"
	@echo "  test          - Run unit tests"
	@echo "  clean         - Remove build artifacts"
	@echo "  gradle-build  - Build using Gradle (KMP)"
	@echo "  setup         - Show setup requirements"
