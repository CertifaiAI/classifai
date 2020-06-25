@echo on
SET JAR="classifai-uberjar/target/classifai-uberjar-1.0-SNAPSHOT-dev.jar"
call mvn -Puberjar -Dmaven.test.skip=true clean package

set "portnumber=%~2"
if "%portnumber%"=="" set "portnumber={port}"

java -jar %JAR% --port=%portnumber%