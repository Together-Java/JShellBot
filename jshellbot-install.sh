#!/usr/bin/env bash

BASE_DIR=/opt/jshellbot/

gradle clean
if gradle fatJar; then
  #move all jar files to the new location
  pattern="build/libs/org.togetherjava.*.jar"
  files=( $pattern )
  echo "${files[0]} copied"
  cp ${files[0]} $BASE_DIR/jshellbot.jar
  echo "Installation Successful"
else
  echo "Failed to build and deploy jshellbot"
fi
