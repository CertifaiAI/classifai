#!/usr/bin/env bash

export JAR="classifai-uberjar/target/classifai-uberjar-1.0-SNAPSHOT-dev.jar"

./mvnw -Puberjar -Dmaven.test.skip=true clean package
java -jar $JAR $1