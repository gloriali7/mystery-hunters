#!/bin/bash
# Double-click this file in Finder to build the browser version of the game.
# It makes a "web-dist" folder (and web-dist.zip) ready to upload to AWS Amplify.

cd "$(dirname "$0")"

JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
if [ -z "$JAVA_HOME" ]; then
    echo "No Java JDK found. Install one from https://adoptium.net and try again."
    read -n 1 -s -r -p "Press any key to close."
    exit 1
fi

echo "Building the web version with $JAVA_HOME ..."
rm -rf build/web web-dist web-dist.zip
mkdir -p build/web/stories web-dist

# CheerpJ runs Java 8 code, so compile for Java 8.
if ! "$JAVA_HOME/bin/javac" --release 8 -d build/web src/*.java; then
    echo
    echo "The code didn't compile. See the errors above."
    read -n 1 -s -r -p "Press any key to close."
    exit 1
fi

# Pack the stories into the jar, with a list of them in file-name order.
cp stories/*.txt build/web/stories/
(cd stories && ls *.txt) > build/web/stories/index.txt

"$JAVA_HOME/bin/jar" --create --file web-dist/mystery-hunters.jar --main-class App -C build/web .
cp web/index.html web-dist/
(cd web-dist && zip -q -r ../web-dist.zip .)

echo
echo "Done! Upload web-dist.zip to AWS Amplify (Deploy without Git)."
read -n 1 -s -r -p "Press any key to close."
