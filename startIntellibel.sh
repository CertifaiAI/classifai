#!/usr/bin/env bash

export JAR="intellibel-uberjar/target/intellibel-uberjar-1.0-SNAPSHOT-dev.jar"

./mvnw -Puberjar -Dmaven.test.skip=true clean package
java -jar $JAR $1