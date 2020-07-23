@echo off

setlocal
:PROMPT
SET /P AREYOUSURE="Continue to install Classifai [Y/N]? "
IF /I "%AREYOUSURE%" == "N" GOTO ABORT
IF /I "%AREYOUSURE%" == "Y" GOTO INSTALL
goto CLOSE

:INSTALL
rem install classifai program
mkdir %APPDATA%\classifai

copy classifai.bat %APPDATA%\classifai
copy classifai-win-1.0-SNAPSHOT-dev.jar %APPDATA%\classifai
copy Classifai_Favicon_Dark_512px.ico %APPDATA%\classifai

rem get %desktop%

if exist %USERPROFILE%\Desktop (
    SET DESKTOP=%USERPROFILE%\Desktop
rem ) else if exist C:\Users\Admin\OneDrive\Desktop (
rem    SET DESKTOP=C:\Users\Admin\OneDrive\Desktop
) else (
    echo Desktop path not found
    goto :ABORT
)

rem create classifai shortcut

set SCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
echo sLinkFile = "%DESKTOP%\classifai.lnk" >> %SCRIPT%
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %SCRIPT%
echo oLink.TargetPath = "%APPDATA%\classifai\classifai.bat" >> %SCRIPT%
echo oLink.IconLocation = "%APPDATA%\classifai\Classifai_Favicon_Dark_512px.ico" >> %SCRIPT%
echo oLink.Save >> %SCRIPT%

cscript /nologo %SCRIPT%
del %SCRIPT%

echo Installation of Classifai completed. Success!

rem start classifai
endlocal
goto CLOSE

:ABORT
echo Installation of Classifai abort.
echo File an issue on https://github.com/CertifaiAI/classifai
echo or contact dev team through email helloannotation@certifai.ai.
goto CLOSE

:CLOSE
pause

