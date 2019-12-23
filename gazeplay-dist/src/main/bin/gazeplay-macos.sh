#!/bin/sh

set -e

export JAVA_OPTS="-Xms256m -Xmx1g"

CLASSPATH=$(find ../lib -name "*.jar" | sort | tr '\n' ':')

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher"

echo "Executing command line: $JAVA_CMD"

${JAVA_CMD}
