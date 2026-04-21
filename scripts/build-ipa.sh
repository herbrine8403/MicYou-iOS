#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

xcodegen generate

PBXPROJ="MicYou-iOS.xcodeproj/project.pbxproj"
if [ -f "$PBXPROJ" ]; then
  perl -0pi -e 's/objectVersion = 77;/objectVersion = 56;/g; s/LastUpgradeCheck = 77[0-9]{2};/LastUpgradeCheck = 1540;/g' "$PBXPROJ"
fi

# Unsigned CI build path (no Apple Developer account required):
# Build for iOS Simulator and collect the .app bundle.
xcodebuild -project MicYou-iOS.xcodeproj -scheme MicYouIOS -configuration Release -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 15' CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO build

mkdir -p build/unsigned
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -type d -path '*Build/Products/Release-iphonesimulator/*.app' -maxdepth 8 2>/dev/null | head -n 1 || true)
if [ -n "$APP_PATH" ] && [ -d "$APP_PATH" ]; then
  rm -rf build/unsigned/MicYouIOS.app
  cp -R "$APP_PATH" build/unsigned/MicYouIOS.app
fi

# Keep archive attempt for logs/debugging only; failure should not fail the job.
xcodebuild -project MicYou-iOS.xcodeproj -scheme MicYouIOS -configuration Release -archivePath build/MicYouIOS.xcarchive CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO archive || true
