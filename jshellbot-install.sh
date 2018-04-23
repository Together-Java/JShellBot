#!/usr/bin/env bash

BASE_DIR=/opt/jshellbot/
token="missing"

read -p "Enter your bot token here: " token
cp src/main/resources/bot.properties.example src/main/resources/bot.properties
echo "$(grep -v "token=yourtokengoeshere" src/main/resources/bot.properties)" > src/main/resources/bot.properties
echo "token=$token" >> src/main/resources/bot.properties

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
