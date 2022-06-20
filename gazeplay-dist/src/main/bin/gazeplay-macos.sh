#!/bin/sh

set -e

export JAVA_OPTS="-Xms256m -Xmx1g --add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED -Dlogging.appender.console.level=OFF"

CLASSPATH=$(find ../lib -name "*.jar" | sort | tr '\n' ':')

export JAVA_HOME=../lib/jre

echo "JAVA_HOME = ${JAVA_HOME}"

export PATH=${JAVA_HOME}/bin:${PATH}

echo "PATH = ${PATH}"

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher"

echo "Executing command line: $JAVA_CMD"

chmod -R 777 ./../lib/

${JAVA_CMD}
