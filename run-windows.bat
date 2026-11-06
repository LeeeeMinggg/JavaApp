@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul || (echo [ERROR] Chua tim thay Java 17+ trong PATH. & pause & exit /b 1)
where mvn >nul 2>nul || (echo [ERROR] Chua tim thay Maven trong PATH. & pause & exit /b 1)
mvn javafx:run
if errorlevel 1 pause
endlocal
