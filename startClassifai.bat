@REM Copyright (c) 2020-2021 CertifAI Sdn. Bhd.

@echo on
SET JAR="%userprofile%\.m2\repository\ai\classifai\classifai-uberjar\2.0.0-alpha1\classifai-uberjar-2.0.0-alpha1-dev.jar"

del %JAR%

call mvnw -Puberjar -Dmaven.test.skip=true clean install

java -jar -Dlog.dir=%CLASSIFAI_HOME% %JAR% %*
