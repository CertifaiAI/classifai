@ECHO OFF

SET OUTPUT_DIR="output"

REM clean up output
rmdir /s/q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%

REM jlink --output java8_runtime --add-modules java.se --module-path "C:\Program Files\AdoptOpenJDK\jdk-8.0.262.10-hotspot"

REM
jpackage --type msi^
         --name classifai^
         --description "Classifai installation for windows"^
         --app-version 1.0.0^
         --input input --dest output^
         --main-jar classifai-uberjar-1.0-SNAPSHOT-dev.jar^
         --main-class ai.classifai.ClassifaiApp^
         --arguments --unlockdb=true --arguments --port=9999^
         --resource-dir metadata^
         --description "Classifai data annotation tool"^
         --vendor "CertifAI Sdn. Bhd."^
         --copyright "Copyright 2020, All rights reserved"^
         --verbose^
         --win-shortcut --win-dir-chooser --win-menu
REM --win-console