#!/usr/bin/env bash

CLASSIFAI_VERSION="1.0-SNAPSHOT"
CLASSIFAI_JAR="classifai-uberjar-$CLASSIFAI_VERSION-dev.jar"

export CLASSIFAI_PATH="$HOME/.m2/repository/ai/certifai/classifai-uberjar/$CLASSIFAI_VERSION/$CLASSIFAI_JAR"

./mvnw -Puberjar -Dmaven.test.skip=true clean package

java -jar $CLASSIFAI_PATH $1 $2
