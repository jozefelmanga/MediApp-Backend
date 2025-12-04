@echo off
echo ============================================
echo   MediApp - Stopping All Microservices
echo ============================================
echo.

echo Closing all service windows...

taskkill /FI "WINDOWTITLE eq Discovery Server*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Security Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Gateway Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq User Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Doctor Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Booking Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Notification Service*" /F >nul 2>&1

echo.
echo All service windows have been closed.
echo.
echo Note: If services are still running, you can kill Java processes with:
echo   taskkill /F /IM java.exe
echo.
pause
