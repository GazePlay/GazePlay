#!/bin/sh

set -e

MAIN_JAR_FILE=gazeplay-@VERSION@.jar

export JAVA_OPTS="-Xms256m -Xmx1g --add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED -Dlogging.appender.console.level=OFF"

WORKING_DIR=$(pwd)

echo "WORKING_DIR = ${WORKING_DIR}"

SCRIPT_DIR=$(dirname $0)

echo "SCRIPT_DIR = ${SCRIPT_DIR}"

LIB_DIR=${SCRIPT_DIR}/../lib

echo "LIB_DIR = ${LIB_DIR}"

LIB_DIR_RELATIVE=$(realpath --relative-to="${WORKING_DIR}" "${LIB_DIR}")

echo "LIB_DIR_RELATIVE = ${LIB_DIR_RELATIVE}"

CLASSPATH=$(find ./$LIB_DIR_RELATIVE -name "*.jar" | sort | tr '\n' ':')

export JAVA_HOME=${LIB_DIR}/jre

echo "JAVA_HOME = ${JAVA_HOME}"

export PATH=${JAVA_HOME}/bin:${PATH}

echo "PATH = ${PATH}"

USB_PKG_OK=$(dpkg -s tobiiusbservice | grep "install ok installed")

ENGINE_PKG_OK=$(dpkg -s tobii-engine-linux-ben | grep "install ok installed")

if [ "" = "${USB_PKG_OK}" ] || [ "" = "${ENGINE_PKG_OK}" ]
then
  echo "Tobii4C drivers not installed."
  while true; do
    read -p "Do you wish to install tobii4C drivers?[y/n]" yn
    case $yn in
        [Yy]* )
          cd $WORKING_DIR/../tobiiDrivers/drivers/
          sh ./install_drivers.sh
          cd $WORKING_DIR
          break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
  done
else
  echo "Tobii4C drivers are installed."
fi

export JAVA_CMD="java -cp \"$CLASSPATH\" ${JAVA_OPTS} net.gazeplay.GazePlayLauncher"

echo "Executing command line: $JAVA_CMD"

chmod -R 777 ${LIB_DIR}

${JAVA_CMD}
