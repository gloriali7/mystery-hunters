#!/bin/bash
# Double-click this file in Finder to build and play the game.

cd "$(dirname "$0")"

# Use JDK 11 if it's installed, otherwise the newest JDK on this Mac.
JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
if [ -z "$JAVA_HOME" ]; then
    echo "No Java JDK found. Install one from https://adoptium.net and try again."
    read -n 1 -s -r -p "Press any key to close."
    exit 1
fi

echo "Building Mystery Hunters with $JAVA_HOME ..."
mkdir -p bin
if ! "$JAVA_HOME/bin/javac" -d bin src/*.java; then
    echo
    echo "The code didn't compile. See the errors above."
    read -n 1 -s -r -p "Press any key to close."
    exit 1
fi

echo "Starting the game. Close the game window to quit."
"$JAVA_HOME/bin/java" -cp bin App
