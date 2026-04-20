#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

xcodegen generate
xcodebuild -project MicYou-iOS.xcodeproj -scheme MicYouIOS -configuration Release -archivePath build/MicYouIOS.xcarchive archive
xcodebuild -exportArchive -archivePath build/MicYouIOS.xcarchive -exportPath build/ipa -exportOptionsPlist scripts/export-options.plist
