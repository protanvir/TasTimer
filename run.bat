@echo off
echo Starting Mock Server...
start "RFID Mock Server" /MIN cmd /c "node mock-server\tcp_server.js"
timeout /t 2 > nul
start /WAIT "RFID Timing System" java --enable-native-access=ALL-UNNAMED -jar TimingSoft.jar
taskkill /FI "WINDOWTITLE eq RFID Mock Server" /T /F
