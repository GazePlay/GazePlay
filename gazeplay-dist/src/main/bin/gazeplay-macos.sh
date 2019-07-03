#!/bin/sh

set -e

MAIN_JAR_FILE=gazeplay-${project.version}.jar

export JAVA_OPTS="-Xms256m -Xmx1g"

export JAVA_CMD="java ${JAVA_OPTS} -jar ../lib/${MAIN_JAR_FILE}"

echo "Executing command line: $JAVA_CMD"

${JAVA_CMD}
