@echo on
SET JAR="intellibel-uberjar/target/intellibel-uberjar-1.0-SNAPSHOT-dev.jar"
call mvnw.cmd -Puberjar -Dmaven.test.skip=true clean package
java -jar %JAR% --port=%2