#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPS="$BASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Maven Wrapper jar not found: $WRAPPER_JAR" >&2
  exit 1
fi

exec java -Dmaven.multiModuleProjectDirectory="$BASEDIR" -Dwrapper.conf="$WRAPPER_PROPS" -cp "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
