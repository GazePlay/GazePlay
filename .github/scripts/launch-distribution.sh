#!/usr/bin/env bash
# Starts GazePlay from a distribution with its launch script, as a user would, and checks that after DELAY seconds
# (40 by default) it is still running, has loaded its game catalog and has logged no unexpected error.
#
# Usage: launch-distribution.sh <launch script> <home folder> [arguments of GazePlay...]
# The display is the job of the caller (e.g. "xvfb-run --auto-servernum launch-distribution.sh ..." on Linux).
set -u

launcher=$1
home=$2
shift 2
delay=${DELAY:-40}

rm -rf "$home"
mkdir -p "$home/GazePlay"
# Mouse mode: there is no eye tracker on the CI machines
echo "EYE_TRACKER=mouse_control" > "$home/GazePlay/GazePlay.properties"
console="$home/console.log"
log="$home/GazePlay/gazeplay.log"

# Java takes user.home from the account database, not from $HOME: it is given through JAVA_TOOL_OPTIONS, with the
# GazePlay folder (otherwise in ~/Documents, which the macOS runners have), read by the
# JVM whatever the launch script ($HOME itself is kept, the audio output may need the configuration of the user).
# Started from another folder than bin/, without terminal.
cd "$(mktemp -d)"
JAVA_TOOL_OPTIONS="-Duser.home=$home -Dgazeplay.folder=$home/GazePlay" sh "$launcher" "$@" < /dev/null > "$console" 2>&1 &
pid=$!
sleep "$delay"

running=yes
kill -0 "$pid" 2> /dev/null || running=no

echo "----- console -----"
tail -40 "$console"
echo "----- $log -----"
tail -60 "$log" 2> /dev/null || echo "No log in $log"
echo "-----"

if [ "$running" = no ]; then
  wait "$pid"
  echo "GazePlay stopped (exit code $?)"
  exit 1
fi
# The launch scripts end with exec: the process is the JVM itself
kill "$pid" 2> /dev/null
sleep 3
kill -9 "$pid" 2> /dev/null || true

grep -q "Games found : " "$log" || { echo "The game catalog was not loaded"; exit 1; }
if cat "$console" "$log" | grep -E "Exception| ERROR " | grep -vE "NoSuchFileException|UnixException|at java.base/"; then
  echo "Unexpected errors"
  exit 1
fi
echo "GazePlay was running without error after ${delay} s: $*"
