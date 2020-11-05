@REM Copyright (c) 2020 CertifAI Sdn. Bhd.

@echo on
SET JAR="%userprofile%\.m2\repository\ai\classifai\classifai-uberjar\1.0-SNAPSHOT\classifai-uberjar-1.0-SNAPSHOT-dev.jar"

del %JAR%

call mvnw -Puberjar -Dmaven.test.skip=true clean install

java -jar -Dlog.dir=%CLASSIFAI_HOME% %JAR% %*
