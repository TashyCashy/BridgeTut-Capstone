#!/usr/bin/env bash
# Builds and launches BridgeTut end-to-end: compiles the Java backend,
# starts the BiddingGateway py4j server, waits for it to be ready, then
# launches the Python GUI. Closing the GUI shuts the backend down too.
set -e
cd "$(dirname "$0")"

echo "Locating py4j..."
PY4J_JAR=$(python3 -c "
import sys, os, glob
matches = glob.glob(os.path.join(sys.prefix, 'share', 'py4j', '*.jar'))
print(matches[0] if matches else '')
")

if [ -z "$PY4J_JAR" ]; then
    echo "Could not find the py4j jar. Is py4j installed? (pip install py4j)"
    exit 1
fi
echo "Using py4j jar: $PY4J_JAR"

echo "Compiling Java sources..."
rm -rf out/production/BridgeTut
mkdir -p out/production/BridgeTut
find src -name "*.java" -not -iname "*Test*" -print0 \
    | xargs -0 javac -cp "$PY4J_JAR" -d out/production/BridgeTut

echo "Starting bidding gateway (Java backend)..."
java -cp "out/production/BridgeTut:$PY4J_JAR" BiddingSystem.BiddingGateway > bidding_gateway.log 2>&1 &
GATEWAY_PID=$!

cleanup() {
    echo "Shutting down backend..."
    kill "$GATEWAY_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "Waiting for backend to be ready..."
READY=0
for i in $(seq 1 30); do
    if ! kill -0 "$GATEWAY_PID" 2>/dev/null; then
        echo "Backend process exited unexpectedly - see bidding_gateway.log"
        cat bidding_gateway.log
        exit 1
    fi
    if python3 -c "import socket; socket.create_connection(('127.0.0.1', 25333), timeout=1).close()" 2>/dev/null; then
        READY=1
        break
    fi
    sleep 0.5
done

if [ "$READY" -ne 1 ]; then
    echo "Backend never became reachable on port 25333 - see bidding_gateway.log"
    cat bidding_gateway.log
    exit 1
fi
echo "Backend is up."

echo "Checking database connection..."
python3 -c "
import mysql.connector
try:
    mysql.connector.connect(host='127.0.0.1', port=3306, user='root', password='bridge', database='Bridge').close()
    print('Database reachable.')
except Exception as e:
    print('WARNING: could not reach the MySQL database (' + str(e) + ')')
    print('  Login/game-history features will fail until it is running.')
"

echo "Launching GUI..."
cd src/GUI
python3 GUI.py
