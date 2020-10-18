#!/usr/bin/env bash

rm -rf output
mkdir output

jpackage --type pkg \
         --name classifai \
         --description "Classifai installation for mac" \
         --app-version 1.0.0 \
         --input input --dest output \
         --main-jar classifai-uberjar-1.0-SNAPSHOT-dev.jar \
         --main-class ai.classifai.ClassifaiApp \
         --arguments --unlockdb=true --arguments --port=9999 \
         --resource-dir metadata \
         --verbose