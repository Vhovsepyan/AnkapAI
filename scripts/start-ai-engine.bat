echo Starting AI Engine...
start "AI Engine" cmd /k ^
cd /d C:\Users\vaheh\IdeaProjects\ankap\ai-engine ^& ^
call venv\Scripts\activate ^& ^
uvicorn app.main:app --port 8001

timeout /t 3 >nul