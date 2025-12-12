@echo off
echo ==========================================
echo Restarting ANKAP Trading System
echo ==========================================

REM ---------- Stop previous processes ----------
echo Stopping previous services...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM python.exe >nul 2>&1

timeout /t 2 >nul

REM ---------- Start AI Engine ----------
echo Starting AI Engine...
start "AI Engine" cmd /k ^
cd /d C:\Users\vaheh\IdeaProjects\ankap\ai-engine ^& ^
call venv\Scripts\activate ^& ^
uvicorn app.main:app --port 8001

timeout /t 3 >nul

REM ---------- Start Trading Engine ----------
echo Starting Trading Engine...
start "Trading Engine" cmd /k ^
cd /d C:\Users\vaheh\IdeaProjects\ankap\trading-engine ^& ^
gradlew.bat bootRun

echo ==========================================
echo All services started.
echo ==========================================
