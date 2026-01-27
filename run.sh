#!/bin/bash
# Start the mock server in the background
node mock-server/server.js > /dev/null 2>&1 &
SERVER_PID=$!

# Ensure the server is killed when the script exits
trap "kill $SERVER_PID" EXIT

# Wait a moment for the server to start
sleep 2

java -jar TimingSoft.jar
