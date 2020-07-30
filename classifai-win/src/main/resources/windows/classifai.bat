@ECHO OFF

REM set classifai jar name
SET CLASSIFAI_JAR=classifai-uberjar-1.0-SNAPSHOT-dev.jar

REM set installation path
SET INSTALLED_PATH=%APPDATA%\classifai\%CLASSIFAI_JAR%

IF EXIST "%INSTALLED_PATH%" (
    start java -jar -Dlog.dir=%CLASSIFAI_HOME% %INSTALLED_PATH% --unlockdb=true --port=9999
) ELSE (
    REM fall back into finding jar file in current path, log in the same path too
    start java -jar -Dlog.dir=%cd% %CLASSIFAI_JAR% --unlockdb=true --port=9999
)


