.PHONY: help shared-build ios-build clean

help:
	@echo "Targets: shared-build ios-build clean"

shared-build:
	./gradlew :shared:build

ios-build:
	xcodegen generate && xcodebuild -project MicYou-iOS.xcodeproj -scheme MicYouIOS -configuration Release -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 15' CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO build

clean:
	./gradlew clean
