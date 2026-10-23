#!/usr/bin/env bash
# Installs Ubuntu packages on a GitHub runner.
#
# On 2026-10-22, "apt-get update" hung twice on the Azure mirror of Ubuntu used by the runners (once for 2 hours, before
# the jobs had a time limit). Retrying on the same mirror does not help: each attempt is limited in time, and the next
# attempts use the main Ubuntu mirror.
#
# Usage: install-apt-packages.sh <package...>
set -u

apt_options=(-o Acquire::Retries=3 -o Acquire::http::Timeout=30)

for attempt in 1 2 3; do
  if timeout 180 sudo apt-get "${apt_options[@]}" update \
    && timeout 300 sudo apt-get "${apt_options[@]}" install -y "$@"; then
    exit 0
  fi
  echo "::warning::apt-get failed or timed out (attempt $attempt), next attempt on the main Ubuntu mirror"
  # Ubuntu 24.04 lists its mirrors in ubuntu.sources (deb822 format), older versions in sources.list
  for sources in /etc/apt/sources.list.d/ubuntu.sources /etc/apt/sources.list; do
    if [ -f "$sources" ]; then
      sudo sed -i 's|http://azure.archive.ubuntu.com/ubuntu|http://archive.ubuntu.com/ubuntu|g' "$sources"
    fi
  done
  sleep 10
done

echo "::error::apt-get failed 3 times: $*"
exit 1
