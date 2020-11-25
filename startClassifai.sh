#!/usr/bin/env bash

CLASSIFAI_VERSION="1.0-SNAPSHOT"
CLASSIFAI_JAR="classifai-uberjar-$CLASSIFAI_VERSION-dev.jar"

export CLASSIFAI_ABSPATH_JAR="$HOME/.m2/repository/ai/classifai/classifai-uberjar/$CLASSIFAI_VERSION/$CLASSIFAI_JAR"

rm $CLASSIFAI_ABSPATH_JAR

./mvnw -Puberjar -Dmaven.test.skip=true clean install

java -jar $CLASSIFAI_ABSPATH_JAR $1 $2
