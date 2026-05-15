#!/usr/bin/env sh

#
# Minimal Gradle wrapper script.
# Note: This repo includes two wrapper JARs extracted from the Gradle distribution:
#  - gradle-wrapper.jar
#  - gradle-wrapper-shared.jar
#

APP_HOME=$(cd "$(dirname "$0")" && pwd)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar:$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar:$APP_HOME/gradle/wrapper/gradle-cli.jar"

JAVA_EXEC="${JAVA_HOME:-}/bin/java"
if [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC="java"
fi

exec "$JAVA_EXEC" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
