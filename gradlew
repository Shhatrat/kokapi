#!/bin/sh
#
# Gradle start up script for UN*X
#
DIRNAME=$(dirname "$0")
APP_HOME=$(cd "$DIRNAME" >/dev/null; pwd)
APP_NAME="Gradle"

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

set -- \
        "-Dorg.gradle.appname=$APP_HOME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

exec java "$@"
