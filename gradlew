#!/usr/bin/env sh

# Minimal Gradle wrapper fallback for environments without a checked-in wrapper.
# Tries the local Gradle installation first, then falls back to a mirror-downloaded wrapper
# if configured externally.

set -e

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPS="gradle/wrapper/gradle-wrapper.properties"

if [ -f "$WRAPPER_JAR" ] && [ -f "$WRAPPER_PROPS" ]; then
  exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

echo "Gradle is not installed and the wrapper files are missing." >&2
echo "Please add a standard Gradle wrapper or install Gradle locally." >&2
exit 1
