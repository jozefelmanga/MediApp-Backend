#!/bin/bash

echo "============================================"
echo "  MediApp - Starting All Microservices"
echo "============================================"
echo ""

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Function to start a service in a new terminal
start_service() {
    local name=$1
    local dir=$2
    local port=$3
    
    echo "Starting $name on port $port..."
    
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --title="$name" -- bash -c "cd '$PROJECT_DIR/$dir' && ./mvnw spring-boot:run; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -T "$name" -e "cd '$PROJECT_DIR/$dir' && ./mvnw spring-boot:run; exec bash" &
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -p tabtitle="$name" -e bash -c "cd '$PROJECT_DIR/$dir' && ./mvnw spring-boot:run; exec bash" &
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        osascript -e "tell app \"Terminal\" to do script \"cd '$PROJECT_DIR/$dir' && ./mvnw spring-boot:run\""
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        # Git Bash on Windows
        start "$name" bash -c "cd '$PROJECT_DIR/$dir' && ./mvnw spring-boot:run"
    else
        echo "Could not detect terminal. Starting $name in background..."
        cd "$PROJECT_DIR/$dir" && ./mvnw spring-boot:run &
    fi
}

echo "[1/7] Starting Discovery Server (Eureka) on port 8761..."
start_service "Discovery Server" "discovery-server" "8761"
echo "Waiting for Discovery Server to initialize..."
sleep 20

echo "[2/7] Starting Security Service on port 8080..."
start_service "Security Service" "security-service" "8080"
sleep 10

echo "[3/7] Starting Gateway Service on port 8550..."
start_service "Gateway Service" "gateway-service" "8550"
sleep 10

echo "[4/7] Starting User Service on port 8666..."
start_service "User Service" "user-service" "8666"
sleep 10

echo "[5/7] Starting Doctor Service (random port)..."
start_service "Doctor Service" "doctor-service" "random"
sleep 10

echo "[6/7] Starting Booking Service on port 8084..."
start_service "Booking Service" "booking-service" "8084"
sleep 10

echo "[7/7] Starting Notification Service on port 8667..."
start_service "Notification Service" "notification-service" "8667"

echo ""
echo "============================================"
echo "  All services are starting!"
echo "============================================"
echo ""
echo "Service URLs:"
echo "  - Eureka Dashboard:     http://localhost:8761"
echo "  - API Gateway:          http://localhost:8550"
echo "  - Security Service:     http://localhost:8080"
echo "  - User Service:         http://localhost:8666"
echo "  - Booking Service:      http://localhost:8084"
echo "  - Notification Service: http://localhost:8667"
echo "  - Doctor Service:       (Check Eureka for port)"
echo ""
echo "Press Enter to exit..."
read
