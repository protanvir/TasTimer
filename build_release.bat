@echo off
echo Building RFID Timing System Release...

REM --- Clean Up ---
if exist dist rmdir /s /q dist
mkdir dist
mkdir dist\lib
mkdir dist\data

REM --- Copy Dependencies ---
echo Copying libraries...
copy lib\octane-sdk.jar dist\lib\
copy lib\Symbol.RFID.API3.jar dist\lib\
copy RFIDAPI32PC.dll dist\
copy lib\*.so dist\

REM --- Compile ---
echo Compiling source code...
if not exist bin mkdir bin
javac -cp "lib/*" -d bin src/*.java

REM --- Package JAR ---
echo Packaging JAR...
"C:\Program Files\Java\jdk-25.0.2\bin\jar.exe" cvfm dist\TimingSoft.jar MANIFEST.MF -C bin .

echo Copying mock-server...
xcopy /E /I /Y mock-server dist\mock-server

REM --- Create Run Script ---
echo Creating run.bat...
echo @echo off > dist\run.bat
echo echo Starting Mock Server... >> dist\run.bat
echo start "RFID Mock Server" /MIN cmd /c "node mock-server\tcp_server.js" >> dist\run.bat
echo timeout /t 2 > nul >> dist\run.bat
echo start /WAIT "RFID Timing System" java -jar TimingSoft.jar >> dist\run.bat
echo taskkill /FI "WINDOWTITLE eq RFID Mock Server" /T /F >> dist\run.bat

echo Copying run.sh...
copy run.sh dist\

echo.
echo ==========================================
echo Build Complete!
echo You can find the portable app in the 'dist' folder.
echo ==========================================
pause
