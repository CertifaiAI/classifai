@echo off
rem install classifai program
mkdir %APPDATA%\classifai

copy classifai.bat %APPDATA%\classifai
copy classifai-uberjar-1.0-SNAPSHOT-dev.jar %APPDATA%\classifai

rem get %desktop% 

for /f "usebackq tokens=3*" %%D IN (`reg query "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\User Shell Folders" /v Desktop`) do set DESKTOP=%%D

if not defined %DESKTOP% SET DESKTOP=%USERPROFILE%\Desktop

echo %DESKTOP%

rem create classifai shortcut

set SCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
echo sLinkFile = "%DESKTOP%\classifai.lnk" >> %SCRIPT%
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %SCRIPT%
echo oLink.TargetPath = "%APPDATA%\classifai\classifai.bat" >> %SCRIPT%
echo oLink.Save >> %SCRIPT%

cscript /nologo %SCRIPT%
del %SCRIPT%