REM ---------- Stop previous processes ----------
echo stopping AI-ENGINE services...
taskkill /F /IM python.exe >nul 2>&1
timeout /t 2 >nul