@echo off

start java -jar %APPDATA%\classifai\classifai-uberjar-1.0-SNAPSHOT-dev.jar --unlockdb=true --port=9999
rem set url="http://localhost:9999/"
rem start chrome %url%
