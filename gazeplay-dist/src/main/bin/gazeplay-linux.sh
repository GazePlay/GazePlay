#!/bin/sh
# GazePlay launcher (Linux x64)
set -e

# Paths are resolved from the location of this script, whatever the working directory
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
LIB_DIR="${SCRIPT_DIR}/../lib"

# Bundled runtime when present (distributions with a JRE), java of the system otherwise (no-JRE distribution).
# Zip archives do not keep the executable bit: restore it for the owner only (it used to be "chmod -R 777 lib"
# at each launch, which made the jars writable by every user and failed when GazePlay was installed by root).
JAVA_CMD=java
if [ -d "${LIB_DIR}/jre/bin" ]; then
  JAVA_HOME="${LIB_DIR}/jre"
  export JAVA_HOME
  [ -x "${JAVA_HOME}/bin/java" ] || chmod u+x "${JAVA_HOME}/bin/"* 2>/dev/null || true
  JAVA_CMD="${JAVA_HOME}/bin/java"
fi

# Recursive: also finds jars placed in sub-folders of lib
CLASSPATH=$(find "${LIB_DIR}" -name "*.jar" | sort | tr '\n' ':')

# Tobii 4C drivers (Linux x64): optional, GazePlay also works with the mouse. The check must not stop the script
# when dpkg or the packages are missing, and the question is only asked from a terminal.
if command -v dpkg >/dev/null 2>&1 && [ -t 0 ]; then
  if ! dpkg -s tobiiusbservice 2>/dev/null | grep -q "install ok installed" \
    || ! dpkg -s tobii-engine-linux-ben 2>/dev/null | grep -q "install ok installed"; then
    echo "Tobii 4C drivers not installed."
    while true; do
      printf "Do you wish to install Tobii 4C drivers? [y/n] "
      read -r yn || break
      case $yn in
        [Yy]*)
          (cd "${SCRIPT_DIR}/../tobiiDrivers/drivers" && sh ./install_drivers.sh)
          break ;;
        [Nn]*) break ;;
        *) echo "Please answer yes or no." ;;
      esac
    done
  fi
fi

# The classpath is passed as one argument (it used to keep literal quotes, losing its first jar)
exec "${JAVA_CMD}" -Xms256m -Xmx1g \
  --add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED \
  -Dlogging.appender.console.level=OFF \
  -cp "${CLASSPATH}" net.gazeplay.GazePlayLauncher "$@"
