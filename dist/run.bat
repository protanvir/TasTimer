@echo off 
echo Starting Mock Server... 
start "RFID Mock Server" /MIN cmd /c "node mock-server\server.js" 
timeout /t 2 
start "RFID Timing System" java -jar TimingSoft.jar 
