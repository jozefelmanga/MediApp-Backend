@echo off
echo ============================================
echo   MediApp - Starting All Microservices
echo ============================================
echo.

set PROJECT_DIR=%~dp0

echo [1/7] Starting Discovery Server (Eureka) on port 8761...
start "Discovery Server" cmd /k "cd /d %PROJECT_DIR%discovery-server && mvnw.cmd spring-boot:run"
echo Waiting for Discovery Server to initialize...
timeout /t 20 /nobreak >nul

echo [2/7] Starting Security Service on port 8080...
start "Security Service" cmd /k "cd /d %PROJECT_DIR%security-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

echo [3/7] Starting Gateway Service on port 8550...
start "Gateway Service" cmd /k "cd /d %PROJECT_DIR%gateway-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

echo [4/7] Starting User Service on port 8666...
start "User Service" cmd /k "cd /d %PROJECT_DIR%user-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

echo [5/7] Starting Doctor Service (random port)...
start "Doctor Service" cmd /k "cd /d %PROJECT_DIR%doctor-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

echo [6/7] Starting Booking Service on port 8084...
start "Booking Service" cmd /k "cd /d %PROJECT_DIR%booking-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

echo [7/7] Starting Notification Service on port 8667...
start "Notification Service" cmd /k "cd /d %PROJECT_DIR%notification-service && mvnw.cmd spring-boot:run"

echo.
echo ============================================
echo   All services are starting!
echo ============================================
echo.
echo Service URLs:
echo   - Eureka Dashboard:    http://localhost:8761
echo   - API Gateway:         http://localhost:8550
echo   - Security Service:    http://localhost:8080
echo   - User Service:        http://localhost:8666
echo   - Booking Service:     http://localhost:8084
echo   - Notification Service: http://localhost:8667
echo   - Doctor Service:      (Check Eureka for port)
echo.
echo Press any key to exit this window...
pause >nul
