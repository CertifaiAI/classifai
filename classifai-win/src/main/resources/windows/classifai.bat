@echo on

rem echo %AppData%\Roaming\classifai
rem set logpath=%AppData%\Roaming\classifai

rem echo ${logpath}
rem pause

rem start java -DLOG_PATH=${logpath} -jar %APPDATA%\classifai\classifai-win-1.0-SNAPSHOT-dev.jar  --unlockdb=true --port=9999

start java -jar -Dlog.dir=%CLASSIFAI_HOME% %APPDATA%\classifai\classifai-win-1.0-SNAPSHOT-dev.jar  --unlockdb=true --port=9999

