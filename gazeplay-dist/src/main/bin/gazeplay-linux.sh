#!/bin/sh

set -e

MAIN_JAR_FILE=gazeplay-@VERSION@.jar

export JAVA_OPTS="-Xms256m -Xmx1g"
export JAVA_OPTS="$JAVA_OPTS -Dlogging.appender.console.level=WARN"

WORKING_DIR=$(pwd)

echo "WORKING_DIR = ${WORKING_DIR}"

SCRIPT_DIR=$(dirname $0)

echo "SCRIPT_DIR = ${SCRIPT_DIR}"

LIB_DIR=${SCRIPT_DIR}/../lib

echo "LIB_DIR = ${LIB_DIR}"

LIB_DIR_RELATIVE=$(realpath --relative-to="${WORKING_DIR}" "${LIB_DIR}")

echo "LIB_DIR_RELATIVE = ${LIB_DIR_RELATIVE}"

CLASSPATH=$(find ./$LIB_DIR_RELATIVE -name "*.jar" | sort | tr '\n' ':')

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher"

echo "Executing command line: $JAVA_CMD"

${JAVA_CMD}
