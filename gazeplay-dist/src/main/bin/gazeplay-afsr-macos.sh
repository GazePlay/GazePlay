#!/bin/sh

set -e

export JAVA_OPTS="-Xms256m -Xmx1g"
export JAVA_OPTS="$JAVA_OPTS -Dlogging.appender.console.level=OFF"

CLASSPATH=$(find ../lib -name "*.jar" | sort | tr '\n' ':')

export JAVA_HOME=../lib/jre

echo "JAVA_HOME = ${JAVA_HOME}"

export PATH=${JAVA_HOME}/bin:${PATH}

echo "PATH = ${PATH}"

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher --afsrgazeplay"

echo "Executing command line: $JAVA_CMD"

${JAVA_CMD}
