#!/bin/sh

set -e

MAIN_JAR_FILE=gazeplay-@VERSION@.jar

WORKING_DIR=$(pwd)
SCRIPT_DIR=$(dirname $0)
LIB_DIR=${SCRIPT_DIR}/../lib
LIB_DIR_RELATIVE=$(realpath --relative-to="${WORKING_DIR}" "${LIB_DIR}")
CLASSPATH=$(find ./$LIB_DIR_RELATIVE -name "*.jar" | sort | tr '\n' ':')

export JAVA_OPTS="-Xms256m -Xmx1g --add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED -Dlogging.appender.console.level=OFF"
export JAVA_HOME=${LIB_DIR}/jre
export PATH=${JAVA_HOME}/bin:${PATH}
export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher --bera"

chmod -R 777 ${LIB_DIR}

${JAVA_CMD}
