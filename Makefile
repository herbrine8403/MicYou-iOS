.PHONY: help shared-build ios-sim-build clean

help:
	@echo "Targets: shared-build ios-sim-build clean"

shared-build:
	./gradlew :shared:build

ios-sim-build:
	./gradlew :iosApp:build

clean:
	./gradlew clean
