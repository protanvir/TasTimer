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

REM --- Compile ---
echo Compiling source code...
if not exist bin mkdir bin
javac -cp lib/octane-sdk.jar -d bin src/*.java

REM --- Package JAR ---
echo Packaging JAR...
"C:\Program Files\Java\jdk-25.0.2\bin\jar.exe" cvfm dist\TimingSoft.jar MANIFEST.MF -C bin .

REM --- Create Run Script ---
echo Creating run.bat...
echo @echo off > dist\run.bat
echo start "RFID Timing System" java -jar TimingSoft.jar >> dist\run.bat

echo Copying run.sh...
copy run.sh dist\

echo.
echo ==========================================
echo Build Complete!
echo You can find the portable app in the 'dist' folder.
echo ==========================================
pause
