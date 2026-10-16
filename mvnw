#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script for Unix/Linux/Git Bash
# ----------------------------------------------------------------------------

set -e

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
darwin=false
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  MINGW*) mingw=true ;;
esac

## Resolve links
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

saveddir=`pwd`
MAVEN_BASEDIR=`dirname "$PRG"`
cd "$MAVEN_BASEDIR"

# Determine Java command
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1 ; then
  echo "Error: JAVA_HOME is not defined correctly or java command cannot be found." >&2
  exit 1
fi

WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROP=".mvn/wrapper/maven-wrapper.properties"

# Read distributionUrl from wrapper properties
DIST_URL=""
if [ -f "$WRAPPER_PROP" ]; then
  DIST_URL=$(grep '^distributionUrl=' "$WRAPPER_PROP" | cut -d= -f2-)
fi

if [ -z "$DIST_URL" ]; then
  DIST_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
fi

# Determine wrapper cache dir
USER_HOME="${HOME:-$USERPROFILE}"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$USER_HOME/.m2}"
MVN_VERSION=$(echo "$DIST_URL" | sed -E 's/.*apache-maven-([0-9.]+)-bin.zip/\1/')
TARGET_DIR="$MAVEN_USER_HOME/wrapper/dists/apache-maven-$MVN_VERSION"
MVN_BIN="$TARGET_DIR/apache-maven-$MVN_VERSION/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  echo "Downloading Maven $MVN_VERSION..."
  mkdir -p "$TARGET_DIR"
  ZIP_FILE="$TARGET_DIR/apache-maven-$MVN_VERSION-bin.zip"
  
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$ZIP_FILE" "$DIST_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$ZIP_FILE" "$DIST_URL"
  else
    echo "Error: Neither curl nor wget found. Please download $DIST_URL manually to $ZIP_FILE" >&2
    exit 1
  fi

  echo "Unzipping Maven..."
  unzip -q -o "$ZIP_FILE" -d "$TARGET_DIR"
  chmod +x "$MVN_BIN" 2>/dev/null || true
  rm -f "$ZIP_FILE"
fi

cd "$saveddir"
exec "$MVN_BIN" "$@"
