from fastapi import FastAPI
from app.api import router
from app.settings import settings
print("AI SETTINGS:", settings.mode, settings.pct_threshold, settings.min_confidence)


app = FastAPI(title="Ankap AI Engine")
app.include_router(router)
