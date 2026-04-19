.PHONY: help shared-build ios-sim-build clean

GRADLE ?= gradle

help:
	@echo "Targets: shared-build ios-sim-build clean"

shared-build:
	$(GRADLE) :shared:build

ios-sim-build:
	$(GRADLE) :iosApp:build

clean:
	$(GRADLE) clean
