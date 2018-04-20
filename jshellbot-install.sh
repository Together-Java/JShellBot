#!/usr/bin/env bash

if [ -z ${JSBOT_INSTALL+x} ]; then
  echo "JSBOT_INSTALL needs to be set as an environment variable"
  exit 1
fi

BASE_DIR=$JSBOT_INSTALL

gradle clean
if gradle fatJar; then
  #move all jar files to the new location
  pattern="build/libs/org.togetherjava.*.jar"
  files=( $pattern )
  echo "${files[0]} copied"
  cp ${files[0]} $JSBOT_INSTALL/jshellbot.jar
  #copy jshellbot.sh and create service
  sudo cp ./jshellbot.sh /etc/init.d/
  echo "Service installed. To start type `service jshellbot start`"
else
  echo "Failed to install service"
fi
