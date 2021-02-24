@REM Copyright (c) 2020-2021 CertifAI Sdn. Bhd.

@echo on
SET JAR="%userprofile%\.m2\repository\ai\classifai\classifai-uberjar\1.2.0\classifai-uberjar-1.2.0-dev.jar"

del %JAR%

call mvnw -Puberjar -Dmaven.test.skip=true clean install

java -jar -Dlog.dir=%CLASSIFAI_HOME% %JAR% %*
