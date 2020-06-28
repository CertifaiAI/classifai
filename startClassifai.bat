@echo on
SET JAR="%userprofile%\.m2\repository\ai\certifai\classifai\classifai-uberjar\1.0-SNAPSHOT\classifai-uberjar-1.0-SNAPSHOT-dev.jar"
call mvnw -Puberjar -Dmaven.test.skip=true clean install

java -jar %JAR% %*
