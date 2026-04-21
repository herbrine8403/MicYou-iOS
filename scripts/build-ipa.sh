#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

xcodegen generate

PBXPROJ="MicYou-iOS.xcodeproj/project.pbxproj"
if [ -f "$PBXPROJ" ]; then
  perl -0pi -e 's/objectVersion = 77;/objectVersion = 56;/g; s/LastUpgradeCheck = 77[0-9]{2};/LastUpgradeCheck = 1540;/g' "$PBXPROJ"
fi

xcodebuild -project MicYou-iOS.xcodeproj -scheme MicYouIOS -configuration Release -archivePath build/MicYouIOS.xcarchive archive
xcodebuild -exportArchive -archivePath build/MicYouIOS.xcarchive -exportPath build/ipa -exportOptionsPlist scripts/export-options.plist
