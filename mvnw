#!/bin/sh
PRG_DIR=`dirname "$0"`
if [ -f "$PRG_DIR/../mvnw" ]; then
  exec "$PRG_DIR/../mvnw" "$@"
else
  echo "Error: Root mvnw not found." >&2
  exit 1
fi
