#!/bin/sh
#
# Gradle start up script for POSIX compatible shells
#
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

set -e
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
