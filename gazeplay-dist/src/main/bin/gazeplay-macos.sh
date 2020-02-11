#!/bin/sh

set -e

export JAVA_OPTS="-Xms256m -Xmx1g"
export JAVA_OPTS="$JAVA_OPTS -Dlogging.appender.console.level=WARN"

CLASSPATH=$(find ../lib -name "*.jar" | sort | tr '\n' ':')

JAVA_HOME=../lib/jre/bin

echo "JAVA_HOME = ${JAVA_HOME}"

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher"

echo "Executing command line: $JAVA_CMD"

${JAVA_CMD}
